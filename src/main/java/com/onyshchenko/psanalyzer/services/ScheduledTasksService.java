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
import java.util.List;
import java.util.Optional;

@Component
public class ScheduledTasksService {

    @Autowired
    private GameService gameService;
    @Autowired
    private UserService userService;
    @Autowired
    private DocumentParseService documentParseService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTasksService.class);

    private static final String BASE_URL = "https://store.playstation.com/ru-ua/";

    private static final String TELEGRAM_URL = "https://api.telegram.org/bot";
    private static final String NOTIFICATION = "There are discounts on games in your wishList. Check it ^_^ "
            + " /wishlist";
    @Value("${bot.token}")
    private String botToken;
    private int totalPages = 1;

    //    @Scheduled(fixedDelay = 6000000)
    @Scheduled(cron = "0 0 5 * * *", zone = "GMT+2:00")
    public void collectDataAboutGamesByList() {

        LocalDateTime startingTime = LocalDateTime.now();
        LOGGER.info("Entering method [collectMinimalDataAboutGamesScheduledTask].");

        collectDataFromSiteByCategory(UrlCategory.ALL_GAMES);
        collectDataFromSiteByCategory(UrlCategory.SALES);
        collectDataFromSiteByCategory(UrlCategory.VR);
        collectDataFromSiteByCategory(UrlCategory.PS5);

        gettingDetailedInfoAboutGames();
        LocalDateTime finishingTime = LocalDateTime.now();
        LOGGER.info("Collecting data about games finished in [{}] minutes",
                Duration.between(startingTime, finishingTime).toMinutes());
    }

    private void collectDataFromSiteByCategory(UrlCategory urlCategory) {

        LOGGER.info("Collecting data from category [{}]", urlCategory.getCategory());
        for (int page = 1; page <= totalPages; page++) {

            LOGGER.info("Get all prices form page: [{}].", page);
            String url = BASE_URL + urlCategory.getUrl() + page;

            Document document = getDataFromUrlWithJsoup(url);

            if (document == null) {
                continue;
            }
            updatePagesNumberInDocument(document);

            List<Game> games = documentParseService.getInitialInfoAboutGamesFromDocument(document, urlCategory.getCategory());
            gameService.compareCollectedListOfGamesToExisted(games);
        }
    }

    private void updatePagesNumberInDocument(Document document) {
        int pagesInDocument = document.getElementsByClass("ems-sdk-grid-paginator__page-buttons")
                .get(0).childNodes().size();
        String s = document.getElementsByClass("ems-sdk-grid-paginator__page-buttons")
                .get(0).childNodes().get(pagesInDocument - 1).childNode(0).childNode(0).toString();
        totalPages = Integer.parseInt(s);
    }

    //        @Scheduled(fixedDelay = 6000000)
    @Scheduled(cron = "0 0 20 * * *", zone = "GMT+2:00")
    public void checkUsersWishListAndSendNotifications() {

        List<Long> userIds = userService.getAllUsersWithDiscountOnGameInWishlist();

        for (Long userId : userIds) {

            Optional<User> user = userService.findUserById(userId);

            if (user.isPresent() && user.get().getChatId() != null && user.get().isNotification()) {
                String chatId = user.get().getChatId();

                LOGGER.info("Sending message to user [{}].", user.get().getUsername());
                notifyUserByChatId(chatId);
            }
        }
    }

    public Document getDataFromUrlWithJsoup(String address) {

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

        for (String url : urls) {
            Document document = getDataFromUrlWithJsoup(BASE_URL + "product/" + url);

            if (document == null) {
                continue;
            }
            Game gameForUpdating = documentParseService.getDetailedGameInfoFromDocument(document);
            long gameId = gameService.getGameIdByUrl(url);
            gameService.updateGamePatch(gameForUpdating, gameId);
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