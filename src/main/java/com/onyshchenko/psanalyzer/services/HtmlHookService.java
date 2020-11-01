package com.onyshchenko.psanalyzer.services;

import com.onyshchenko.psanalyzer.dao.GameRepository;
import com.onyshchenko.psanalyzer.model.Currency;
import com.onyshchenko.psanalyzer.model.DeviceType;
import com.onyshchenko.psanalyzer.model.Game;
import com.onyshchenko.psanalyzer.model.Genre;
import com.onyshchenko.psanalyzer.model.Price;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

@Component
public class HtmlHookService {

    @Autowired
    private GameService gameService;

    @Autowired
    private GameRepository gameRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlHookService.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter
            .ofPattern("d/M/yyyy");
    private static final String BASE_URL = "https://store.playstation.com/ru-ua/";
    private static final String ALL_GAMES_URL = "category/44d8bb20-653e-431e-8ad0-c0a365f68d2f/";


    public Document getDataFromUrlWithJsoup(String url) throws IOException {

        String address = BASE_URL + url;

        Document doc = Jsoup.connect(address).get();
        LOGGER.info("Document received from site with title {}", doc.title());

        return doc;
    }

    public Game getDetailedGameInfoFromDocument(Document doc) {

        Game gameForUpdating = new Game();
        boolean exceptionCaptured = false;
        try {
            Element element = doc.getElementsByClass("psw-grid-x psw-fill-x psw-l-space-y-m psw-grid-margin-x psw-m-y-0").get(0);

            try {
                String publisher = element.getElementsByAttributeValueContaining("data-qa", "publisher-value")
                        .get(0).childNode(0).toString().replaceAll("\n", "");
                if (publisher.isEmpty()) {
                    LOGGER.info("Publisher for game is empty.");

                }
                gameForUpdating.setPublisher(publisher);
            } catch (Exception ex) {
                exceptionCaptured = true;
                LOGGER.info("Exception while getting information about publisher.");
                ex.printStackTrace();
            }

            try {
                String stringReleaseDate = element.getElementsByAttributeValueContaining("data-qa", "releaseDate-value")
                        .get(0).childNode(0).toString().replaceAll("\n", "");
                if (!stringReleaseDate.isEmpty()) {
                    LocalDate releaseDate = LocalDate.parse(stringReleaseDate, formatter);
                    gameForUpdating.setReleaseDate(releaseDate);
                }
            } catch (Exception ex) {
                exceptionCaptured = true;
                LOGGER.info("Exception while getting information about release Date.");
                ex.printStackTrace();
            }

            //getting versions of ps
            try {
                String platform = element.getElementsByAttributeValueContaining("data-qa", "platform-value")
                        .get(0).childNode(0).toString().replaceAll("\n", "");
                gameForUpdating.getDeviceTypes().add(DeviceType.of(platform));
            } catch (Exception ex) {
                exceptionCaptured = true;
                LOGGER.info("Exception while getting information about platform.");
                ex.printStackTrace();
            }
            //getting genres
            try {
                Element genreElements = element.getElementsByAttributeValueContaining("data-qa", "genre-value").get(0);
                String stringGenreList = genreElements.childNode(0).childNode(0).toString();
                String[] stringGenres = stringGenreList.split(",");

                for (String genre : stringGenres) {
                    if (Character.isWhitespace(genre.charAt(0))) {
                        genre = genre.replaceFirst(" ", "");
                    }
                    gameForUpdating.getGenres().add(Genre.of(genre));
                }
            } catch (Exception ex) {
                exceptionCaptured = true;
                LOGGER.info("Exception while getting information about genres.");
                ex.printStackTrace();
            }

        } catch (Exception e) {
            exceptionCaptured = true;
            LOGGER.info("Exception while parsing data from html");
            e.printStackTrace();
        }

        gameForUpdating.setDetailedInfoFilledIn(!exceptionCaptured);
        return gameForUpdating;

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
                    String priceString = gamePriceString.substring(0, gamePriceString.length() - 4).replaceAll(" ", "");
                    int intPrice = (int) Double.parseDouble(priceString);
                    String currencyString = gamePriceString.substring(gamePriceString.length() - 3);
                    price = new Price(intPrice, getCurrencyFromString(currencyString));
                }

                Game createdGame = new Game(gameName, price, gameSku);
                games.add(createdGame);
            } catch (ParseException e) {
                e.printStackTrace();
                LOGGER.info("Parsing error.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return games;

        // TODO: Implement PS Plus discount price
        // Pay attention to "price-display__price__label". It may contain smth like "Сэкономьте еще 5% благодаря".
//        int amountOfGamesWithPSPlusDiscount = doc.getElementsByClass(psPlusDiscountHtmlClassName).size();
//        HashMap<String, Integer> psPlusPriceList = new HashMap<>();
//        if (amountOfGamesWithPSPlusDiscount > 0) {
//            for (int i = 0; i < amountOfGamesWithPSPlusDiscount; i++) {
//                try {
//                    String psPlusStringPrice = doc.getElementsByClass(psPlusDiscountHtmlClassName).get(i).childNode(0).toString();
//                    int convertedPsPlusPrice = (int) Math.round(Double.parseDouble(psPlusStringPrice.substring(1, 7)));
//                    String gameUrl = doc.getElementsByClass(psPlusDiscountHtmlClassName).get(i).parentNode().parentNode().attributes().get("href");
//                    String separatedGameUrl = gameUrl.split("/", 4)[3];
//
//                    psPlusPriceList.put(separatedGameUrl, convertedPsPlusPrice);
//                } catch (Exception ex) {
//                    logger.info("Exception while converting");
//                }
//            }
//        }

    }

    private Currency getCurrencyFromString(String cur) {
        switch (cur) {
            case "UAH":
                return Currency.UAH;
            default:
                throw new IllegalArgumentException("Unrecognized price currency.");
        }
    }

//    @Scheduled(fixedDelay = 6000000)
//    public void scheduledTask() throws IOException {
//
//        LOGGER.info("Process of getting games data from url starting.");
//        for (int page = 1; page < 73; page++) {
//            LOGGER.info("Get all prices form page: " + page);
//            Document document = getDataFromUrlWithJsoup(ALL_GAMES_URL + page);
//            List<Game> games = getListOfGamesFromDocument(document);
//            gameService.checkList(games);
//
//        }
//        LOGGER.info("Getting of all prices is done.");
//    }

    @Scheduled(fixedDelay = 60000)
    public void debugScheduledTask() throws IOException {

        LOGGER.info("Process of getting games data from url starting.");
        Document document = getDataFromUrlWithJsoup("category/44d8bb20-653e-431e-8ad0-c0a365f68d2f/1");
        List<Game> games = getListOfGamesFromDocument(document);
        gameService.checkList(games);

        LOGGER.info("Getting of all prices is done.");
    }

    @Scheduled(fixedDelay = 60000)
    public void gettingDetailedInfoAboutGames() {
        LOGGER.info("Starting procedure of getting detailed info about games.");
        List<String> urls;
        urls = gameRepository.urlsOfNotUpdatedGames();

        if (urls.isEmpty()) {
            LOGGER.info("All games have detailed info.");
            return;
        }
        try {
            for (String url : urls) {
                Document document = getDataFromUrlWithJsoup("product/" + url);

                Game gameForUpdating = getDetailedGameInfoFromDocument(document);

                String gameId = gameRepository.getGameIdByUrl(url);
                gameService.updateGamePatch(gameForUpdating, gameId);
            }
        } catch (Exception ex) {
            LOGGER.info("Error while getting Document.");
            ex.printStackTrace();
        }
    }
}