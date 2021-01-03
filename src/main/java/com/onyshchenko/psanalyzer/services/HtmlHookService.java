package com.onyshchenko.psanalyzer.services;

import com.onyshchenko.psanalyzer.model.Game;
import com.onyshchenko.psanalyzer.model.UrlCategory;
import com.onyshchenko.psanalyzer.model.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class HtmlHookService {

    @Autowired
    private GameService gameService;
    @Autowired
    private UserService userService;
    @Autowired
    private DocumentParseService documentParseService;

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlHookService.class);

    private static final String BASE_URL = "https://store.playstation.com/ru-ua/";

    private static final String TELEGRAM_URL = "https://api.telegram.org/bot";
    private static final String NOTIFICATION = "There are discounts on games in your wishList. Check it ^_^ "
            + " /wishlist";
    @Value("${bot.token}")
    private String botToken;
    private int totalPages = 1;

    //    @Scheduled(fixedDelay = 6000000)
    @Scheduled(cron = "0 0 5 * * *", zone = "GMT+2:00")
    public void collectMinimalDataAboutGamesScheduledTask() {

        LocalDateTime startingTime = LocalDateTime.now();
        LOGGER.info("Process of getting games data from url STARTED.");

        collectDataFromCategoryExclusive(UrlCategory.EXCLUSIVE);
        collectDataFromCategory(UrlCategory.ALL_GAMES);
        collectDataFromCategory(UrlCategory.SALES);
        collectDataFromCategory(UrlCategory.VR);
        collectDataFromCategory(UrlCategory.PS5);

        gettingDetailedInfoAboutGames();
        LOGGER.info("Process of getting games data from url FINISHED.");
        LocalDateTime finishingTime = LocalDateTime.now();

        LOGGER.info("Collecting data about games finished in [{}] minutes", Duration.between(startingTime, finishingTime).toMinutes());
    }

    private void collectDataFromCategoryExclusive(UrlCategory category) {
        try {
            Document document = getDataFromUrlWithJsoup(category.getUrl());
            List<String> listOfGameUrls = document.getElementsByAttributeValueMatching("href", "ru-ua/games/").eachAttr("href");

            // TODO: Add parsing of exclusive PS games.
            Set<String> setOfUrls = new HashSet<>(listOfGameUrls);

            LOGGER.info("Collected [{}] of urls.", setOfUrls);
        } catch (IOException e) {
            LOGGER.info("Exception during getting document from page [{}]", category.getUrl());
        }
    }

    private void collectDataFromCategory(UrlCategory urlCategory) {

        LOGGER.info("Collecting data from category [{}]", urlCategory.getCategory());
        for (int page = 1; page <= totalPages; page++) {

            LOGGER.info("Get all prices form page: [{}].", page);
            Document document = null;
            String url = BASE_URL + urlCategory.getUrl() + page;

            try {
                document = getDataFromUrlWithJsoup(url);
            } catch (IOException e) {
                LOGGER.info("Exception during getting document from page [{}]", page);
            }

            if (document != null) {
                int pagesInDocument = document.getElementsByClass("ems-sdk-grid-paginator__page-buttons")
                        .get(0).childNodes().size();
                String s = document.getElementsByClass("ems-sdk-grid-paginator__page-buttons")
                        .get(0).childNodes().get(pagesInDocument - 1).childNode(0).childNode(0).toString();
                totalPages = Integer.parseInt(s);
                List<Game> games = documentParseService.getInitialInfoAboutGamesFromDocument(document, urlCategory.getCategory());
                gameService.checkCollectedListOfGamesToExisted(games);
            }

        }
    }


    //        @Scheduled(fixedDelay = 6000000)
    @Scheduled(cron = "0 0 20 * * *", zone = "GMT+2:00")
    public void checkUsersWishListAndSendNotifications() {

        List<Long> userIds = userService.getAllUsersWithDiscountOnGameInWishlist();

        for (Long userId : userIds) {

            Optional<User> user = userService.findById(userId);

            if (user.isPresent() && user.get().getChatId() != null && user.get().isNotification()) {
                String chatId = user.get().getChatId();

                LOGGER.info("Sending message to user [{}].", user.get().getUsername());
                notifyUserByChatId(chatId);
            }
        }
    }

    public Document getDataFromUrlWithJsoup(String address) throws IOException {

        try {
            LOGGER.info("Getting document from address: [{}]", address);

            Document doc = Jsoup.connect(address).get();
            String docTitle = doc.title();
            LOGGER.info("Document received from site with title: [{}]", docTitle);

            return doc;
        } catch (Exception ex) {
            LOGGER.info("Error while getting Document.");
            return null;
        }
    }

    public void gettingDetailedInfoAboutGames() {
        LOGGER.info("Process of getting detailed info about games STARTED.");
        List<String> urls = gameService.getUrlsOfNotUpdatedGames();

        if (urls == null || urls.isEmpty()) {
            LOGGER.info("All games have detailed info.");
            return;
        }
        LOGGER.info("Collected [{}] games which are not fully filled.", urls.size());

        try {
            for (String url : urls) {
                Document document = getDataFromUrlWithJsoup(BASE_URL + "product/" + url);

                if (document != null) {
                    Game gameForUpdating = documentParseService.getDetailedGameInfoFromDocument(document);

                    String gameId = gameService.getGameIdByUrl(url);
                    gameService.updateGamePatch(gameForUpdating, gameId);
                } else {
                    LOGGER.info("Error occurred while getting game by url [{}]. Skipping updating with detailed info", url);
                }

            }
        } catch (Exception ex) {
            LOGGER.info("Error while getting detailed info about games.");
        }
        LOGGER.info("Process of getting detailed info about games FINISHED.");
    }

    private void notifyUserByChatId(String chatId) {
        try {
            String address = TELEGRAM_URL + botToken + "/sendMessage?chat_id=" + chatId + "&text=" + NOTIFICATION;
            LOGGER.info("SEND MESSAGE ADDRESS: {}", address);
            Jsoup.connect(address).ignoreContentType(true).post();
        } catch (IOException e) {
            LOGGER.info("Exception while sending message to user.");
        }
    }
}