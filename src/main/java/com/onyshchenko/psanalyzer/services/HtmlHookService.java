package com.onyshchenko.psanalyzer.services;

import com.onyshchenko.psanalyzer.model.Currency;
import com.onyshchenko.psanalyzer.model.DeviceType;
import com.onyshchenko.psanalyzer.model.Game;
import com.onyshchenko.psanalyzer.model.Genre;
import com.onyshchenko.psanalyzer.model.Price;
import com.onyshchenko.psanalyzer.model.User;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

@Component
public class HtmlHookService {

    @Autowired
    private GameService gameService;
    @Autowired
    private UserService userService;

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlHookService.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter
            .ofPattern("d/M/yyyy");
    private static final String BASE_URL = "https://store.playstation.com/ru-ua/";
    private static final String ALL_GAMES_URL = "category/44d8bb20-653e-431e-8ad0-c0a365f68d2f/";
    private static final String TELEGRAM_URL = "https://api.telegram.org/bot";
    private static final String NOTIFICATION = "There are discounts on games in your wishList. Check it ^_^ "
            + " /wishlist";
    private static final String DATA_QA = "data-qa";
    @Value("${bot.token}")
    private String botToken;


    public Document getDataFromUrlWithJsoup(String url) throws IOException {

        String address = BASE_URL + url;
        LOGGER.info("Getting document from address: [{}]", address);

        Document doc = Jsoup.connect(address).get();
        String docTitle = doc.title();
        LOGGER.info("Document received from site with title {}", docTitle);

        return doc;
    }

    public Game getDetailedGameInfoFromDocument(Document doc) {

        Game gameForUpdating = new Game();
        boolean exceptionCaptured = false;
        try {
            Element element = doc.getElementsByClass("psw-grid-x psw-fill-x psw-l-space-y-m psw-grid-margin-x psw-m-y-0").get(0);

            String publisher = extractPublisherFromGmeElement(element);
            if (publisher != null && !publisher.isEmpty()) {
                gameForUpdating.setPublisher(publisher);
            } else {
                exceptionCaptured = true;
                LOGGER.debug("Publisher for game is empty.");
            }

            LocalDate releaseDate = extractReleaseDateFromGameElement(element);
            if (releaseDate != null) {
                gameForUpdating.setReleaseDate(releaseDate);
            } else {
                exceptionCaptured = true;
                LOGGER.debug("ReleaseDate for game is empty.");
            }

            DeviceType deviceType = extractDeviceTypeFromGameElemet(element);
            if (deviceType != null) {
                gameForUpdating.getDeviceTypes().add(deviceType);
            } else {
                exceptionCaptured = true;
                LOGGER.debug("DeviceType for game is empty.");
            }

            Set<Genre> gameGenres = extractGenresFromGameElement(element);
            if (gameGenres.isEmpty()) {
                LOGGER.debug("Genres list for game is empty.");
            } else {
                for (Genre genre : gameGenres) {
                    gameForUpdating.getGenres().add(genre);
                }
            }

            gameForUpdating.setDetailedInfoFilledIn(true);
        } catch (Exception e) {
            exceptionCaptured = true;
            LOGGER.info("Exception while parsing data from html");
            e.printStackTrace();
        }

        gameForUpdating.setErrorWhenFilling(exceptionCaptured);
        return gameForUpdating;

    }

    private Set<Genre> extractGenresFromGameElement(Element element) {

        Set<Genre> genres = new HashSet<>();
        try {
            Element genreElements = element.getElementsByAttributeValueContaining(DATA_QA, "genre-value").get(0);
            String stringGenreList = genreElements.childNode(0).childNode(0).toString();
            String[] stringGenres = stringGenreList.split(",");

            for (String genre : stringGenres) {
                if (Character.isWhitespace(genre.charAt(0))) {
                    String cutGenre = genre.replaceFirst(" ", "");
                    genres.add(Genre.of(cutGenre));
                }
            }
        } catch (Exception ex) {
            LOGGER.info("Exception while getting information about genres.");
        }

        return genres;
    }

    private DeviceType extractDeviceTypeFromGameElemet(Element element) {
        DeviceType deviceType = null;

        try {
            String platform = element.getElementsByAttributeValueContaining(DATA_QA, "platform-value")
                    .get(0).childNode(0).toString().replace("\n", "");
            deviceType = DeviceType.of(platform);
        } catch (Exception ex) {
            LOGGER.info("Exception while getting device type.");
        }
        return deviceType;
    }


    private String extractPublisherFromGmeElement(Element element) {
        String publisher = null;
        try {
            publisher = element.getElementsByAttributeValueContaining(DATA_QA, "publisher-value")
                    .get(0).childNode(0).toString().replace("\n", "");
        } catch (Exception ex) {
            LOGGER.info("Exception while getting information about publisher.");
        }
        return publisher;
    }

    private LocalDate extractReleaseDateFromGameElement(Element element) {

        LocalDate releaseDate = null;
        try {
            String stringReleaseDate = element.getElementsByAttributeValueContaining(DATA_QA, "releaseDate-value")
                    .get(0).childNode(0).toString().replace("\n", "");
            if (!stringReleaseDate.isEmpty()) {
                releaseDate = LocalDate.parse(stringReleaseDate, formatter);
            }
        } catch (Exception ex) {
            LOGGER.info("Exception while getting information about release Date.");
        }

        return releaseDate;
    }

    private List<Game> getListOfGamesFromDocument(Document doc) {

        Elements headline = doc.getElementsByClass(
                "ems-sdk-product-tile-link");
        List<Game> games = new ArrayList<>();
        JSONParser parser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
        for (Element element : headline) {
            try {
                JSONObject json = (JSONObject) parser.parse(element.attributes().get("data-telemetry-meta"));

                String gameName = json.getAsString("name");
                String gameSku = json.getAsString("id");
                String gamePriceString = json.getAsString("price");

                Price price;

                if (gamePriceString == null || gamePriceString.equalsIgnoreCase("бесплатно")) {
                    price = new Price();
                } else {
                    String priceString = gamePriceString.substring(0, gamePriceString.length() - 4).replace(" ", "");
                    int intPrice = (int) Double.parseDouble(priceString);
                    String currencyString = gamePriceString.substring(gamePriceString.length() - 3);
                    price = new Price(intPrice, getCurrencyFromString(currencyString));
                }

                Game createdGame = new Game(gameName, price, gameSku);
                games.add(createdGame);
            } catch (ParseException e) {
                LOGGER.info("Parsing error.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return games;

    }

    private Currency getCurrencyFromString(String cur) {
        if ("UAH".equals(cur)) {
            return Currency.UAH;
        }
        throw new IllegalArgumentException("Unrecognized price currency.");
    }

    //    @Scheduled(fixedDelay = 6000000)
    @Scheduled(cron = "0 0 5 * * *", zone = "GMT+2:00")
    public void scheduledTask() throws IOException {

        LOGGER.info("Process of getting games data from url starting.");
        for (int page = 1; page < 74; page++) {
            LOGGER.info("Get all prices form page: [{}].", page);
            Document document = getDataFromUrlWithJsoup(ALL_GAMES_URL + page);
            List<Game> games = getListOfGamesFromDocument(document);
            gameService.checkList(games);
        }

        gettingDetailedInfoAboutGames();
        LOGGER.info("Getting of all prices is done.");
    }

    public void gettingDetailedInfoAboutGames() {
        LOGGER.info("Starting procedure of getting detailed info about games.");
        List<String> urls = gameService.getUrlsOfNotUpdatedGames();

        if (urls == null || urls.isEmpty()) {
            LOGGER.info("All games have detailed info.");
            return;
        }
        LOGGER.info("Collected [{}] games which are not fully filled.", urls.size());

        try {
            for (String url : urls) {
                Document document = getDataFromUrlWithJsoup("product/" + url);

                Game gameForUpdating = getDetailedGameInfoFromDocument(document);

                String gameId = gameService.getGameIdByUrl(url);
                gameService.updateGamePatch(gameForUpdating, gameId);
            }
        } catch (Exception ex) {
            LOGGER.info("Error while getting Document.");
        }
        LOGGER.info("Procedure of getting detailed info about games finished.");
    }

    //    @Scheduled(fixedDelay = 6000000)
    @Scheduled(cron = "0 0 20 * * *", zone = "GMT+2:00")
    public void checkUsersWishListAndSendNotifications() {

        List<Long> userIds = userService.getAllUsersWithDiscountOnGameInWishlist();

        for (Long userId : userIds) {

            Optional<User> user = userService.findById(userId);

            if (user.isPresent() && user.get().getChatId() != null) {
                String chatId = user.get().getChatId();

                LOGGER.info("Sending message to user [{}].", user.get().getUsername());
                notifyUserByChatId(chatId);
            }

        }

    }

    private void notifyUserByChatId(String chatId) {
        try {
            String address = TELEGRAM_URL + botToken + "/sendMessage?chat_id=" + chatId + "&text=" + NOTIFICATION;
            Jsoup.connect(address).post();
        } catch (IOException e) {
            LOGGER.info("Exception while sending message to user.");
        }
    }
}