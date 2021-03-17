package com.onyshchenko.psanalyzer.services.scheduler;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.onyshchenko.psanalyzer.exception.ForbiddenRequestException;
import com.onyshchenko.psanalyzer.model.Game;
import com.onyshchenko.psanalyzer.model.UrlCategory;
import com.onyshchenko.psanalyzer.model.User;
import com.onyshchenko.psanalyzer.services.GameService;
import com.onyshchenko.psanalyzer.services.UserService;
import com.onyshchenko.psanalyzer.services.parser.DocumentParseService;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
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

    //    @Scheduled(fixedDelay = 6000000)
    @Scheduled(cron = "0 0 5 * * *", zone = "GMT+2:00")
    public void collectDataAboutGamesByList() {

        LocalDateTime startingTime = LocalDateTime.now();
        LOGGER.info("Starting scheduled task for collecting games data.");

        try {
            collectDataFromSiteByCategory(UrlCategory.PS4);
            collectDataFromSiteByCategory(UrlCategory.ALL_SALES);
            collectDataFromSiteByCategory(UrlCategory.SALES);
            collectDataFromSiteByCategory(UrlCategory.VR);
            collectDataFromSiteByCategory(UrlCategory.PS5);
        } catch (ForbiddenRequestException exception) {
            LOGGER.error("Forbidden exception captured.", exception);
        }
        LocalDateTime listDataEndTime = LocalDateTime.now();
        LOGGER.info("Collecting minimal data about games from list finished in [{}] minutes",
                Duration.between(startingTime, listDataEndTime).toMinutes());

        try {
            getDetailedInfoAboutAllGames();
        } catch (ForbiddenRequestException exception) {
            LOGGER.error("Forbidden exception captured.", exception);
        }
        LocalDateTime finishingTime = LocalDateTime.now();
        LOGGER.info("Collecting data about games finished in [{}] minutes",
                Duration.between(startingTime, finishingTime).toMinutes());
    }

    private void collectDataFromSiteByCategory(UrlCategory urlCategory) throws ForbiddenRequestException {

        int page = 1;
        boolean isLastPage = false;

        do {
            LOGGER.info("Getting all data form page: [{}].", page);
            String url = BASE_URL + "category/" + urlCategory.getUrl() + "/" + page;

            Document document = getDataFromUrlWithJsoup(url);

            Optional<DocumentContext> documentContext = prepareDocumentContextFromDocument(document);
            if (!documentContext.isPresent()) {
                LOGGER.warn("Received null value instead of DocumentContext from url: [{}]", url);
                continue;
            }

            String size = getDefaultSizeFromPage(page);
            isLastPage = documentParseService.chekIfTheLastPageOfDocument(documentContext.get(), urlCategory.getUrl(), size);

            List<Game> games = documentParseService
                    .getInitialInfoAboutGamesFromDocumentContext(documentContext.get(), urlCategory.getUrl(), size);
            gameService.compareCollectedListOfGamesToExisted(games);
            page++;
        } while (!isLastPage);
    }


    private String getDefaultSizeFromPage(int page) {
        int defaultPageSize = 24;
        int offset = (page - 1) * defaultPageSize;

        return offset + ":" + defaultPageSize;
    }

    private Optional<DocumentContext> prepareDocumentContextFromDocument(Document document) {

        if (document == null) {
            LOGGER.error("Received null value instead of document from url.");
            return Optional.empty();
        }
        Optional<Node> node = document.body().childNodes().stream()
                .filter(e -> e.toString().contains("__NEXT_DATA__"))
                .findFirst()
                .map(e -> e.childNodes().get(0));
        if (!node.isPresent()) {
            LOGGER.error("Collected 0 elements from document.");
            return Optional.empty();
        }
        String jsonString = node.get().toString();
        DocumentContext context = JsonPath.parse(jsonString);

        return Optional.of(context);
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

    public Document getDataFromUrlWithJsoup(String address) throws ForbiddenRequestException {

        try {
            LOGGER.info("Getting document from address: [{}]", address);
            Document doc = Jsoup.connect(address).get();
            String docTitle = doc.title();
            LOGGER.info("Document received from site with title: [{}]", docTitle);

            return doc;
        } catch (HttpStatusException statusException) {
            LOGGER.error("Status of request: [{}].", statusException.getStatusCode(), statusException);
            if (statusException.getStatusCode() == 403) {
                throw new ForbiddenRequestException("Request forbidden from host side.", 403, address);
            }
            return null;
        } catch (Exception ex) {
            LOGGER.error("Error while getting Document.", ex);
            return null;
        }
    }

    public void getDetailedInfoAboutAllGames() throws ForbiddenRequestException {
        LOGGER.info("Process of getting detailed info about games STARTED.");
        List<String> urls = gameService.getUrlsOfAllGames();
        if (urls == null || urls.isEmpty()) {
            LOGGER.error("No games for updating.");
            return;
        }
        LOGGER.info("Collected [{}] games for getting detailed info.", urls.size());

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