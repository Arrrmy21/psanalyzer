package com.onyshchenko.psanalyzer.services;

import com.onyshchenko.psanalyzer.model.Game;
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
public class HtmlHookService {

    @Autowired
    private GameService gameService;
    @Autowired
    private UserService userService;
    @Autowired
    private DocumentParseService documentParseService;

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlHookService.class);

    private static final String BASE_URL = "https://store.playstation.com/ru-ua/";
    private static final String ALL_GAMES_URL = "category/44d8bb20-653e-431e-8ad0-c0a365f68d2f/";
    private static final String SALES = "category/803cee19-e5a1-4d59-a463-0b6b2701bf7c/";
    private static final String TELEGRAM_URL = "https://api.telegram.org/bot";
    private static final String NOTIFICATION = "There are discounts on games in your wishList. Check it ^_^ "
            + " /wishlist";
    @Value("${bot.token}")
    private String botToken;
    private int totalPages = 5;

    //    @Scheduled(fixedDelay = 6000000)
    @Scheduled(cron = "0 0 5 * * *", zone = "GMT+2:00")
    public void collectMinimalDataAboutGamesScheduledTask() throws IOException {

        LocalDateTime startingTime = LocalDateTime.now();
        LOGGER.info("Process of getting games data from url STARTED.");
        for (int page = 1; page < totalPages; page++) {
            LOGGER.info("Get all prices form page: [{}].", page);
            Document document = getDataFromUrlWithJsoup(ALL_GAMES_URL + page);
            List<Game> games = documentParseService.getInitialInfoAboutGamesFromDocument(document);
            gameService.checkCollectedListOfGamesToExisted(games);
        }

        gettingDetailedInfoAboutGames();
        LOGGER.info("Process of getting games data from url FINISHED.");
        LocalDateTime finishingTime = LocalDateTime.now();

        LOGGER.info("Collecting data about games finished in [{}] minutes", Duration.between(startingTime, finishingTime).toMinutes());
    }

    //    @Scheduled(fixedDelay = 6000000)
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

    public Document getDataFromUrlWithJsoup(String url) throws IOException {

        String address = BASE_URL + url;
        LOGGER.info("Getting document from address: [{}]", address);

        Document doc = Jsoup.connect(address).get();
        String docTitle = doc.title();
        LOGGER.info("Document received from site with title {}", docTitle);

        return doc;
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
                Document document = getDataFromUrlWithJsoup("product/" + url);

                Game gameForUpdating = documentParseService.getDetailedGameInfoFromDocument(document);

                String gameId = gameService.getGameIdByUrl(url);
                gameService.updateGamePatch(gameForUpdating, gameId);
            }
        } catch (Exception ex) {
            LOGGER.info("Error while getting Document.");
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