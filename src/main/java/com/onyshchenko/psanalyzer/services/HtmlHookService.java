package com.onyshchenko.psanalyzer.services;

import com.onyshchenko.psanalyzer.dao.GameRepository;
import com.onyshchenko.psanalyzer.model.Category;
import com.onyshchenko.psanalyzer.model.Currency;
import com.onyshchenko.psanalyzer.model.DeviceType;
import com.onyshchenko.psanalyzer.model.Game;
import com.onyshchenko.psanalyzer.model.Genre;
import com.onyshchenko.psanalyzer.model.Price;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class HtmlHookService {

    @Autowired
    private GameService gameService;

    @Autowired
    private GameRepository gameRepository;

    private static final Logger logger = LoggerFactory.getLogger(HtmlHookService.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("ru"));
    private static final String psPlusDiscountHtmlClassName = "price-display__price--is-plus-upsell";
    private static final String BASE_URL = "https://store.playstation.com/ru-ua/";

    public void getDataFromUrlWithJsoup(String url) throws IOException {

        String address = BASE_URL + url;

        Document doc = Jsoup.connect(address).get();
        logger.info("Doc title {}", doc.title());

        List<Game> games = getListOfGamesFromHtml(doc);

        gameService.checkList(games);
    }

    public void getDataFromUrlWithSelenium(Document doc, String gameId) {

        Game gameForUpdating = new Game();
        try {
            String stringReleaseDate = doc.getElementsByClass("provider-info__list-item").get(1).childNode(0).toString();
            String releaseDateWithoutSpaces = subString(stringReleaseDate).toLowerCase();
            if (!releaseDateWithoutSpaces.isEmpty()) {
                LocalDate releaseDate = LocalDate.parse(releaseDateWithoutSpaces, formatter);
                gameForUpdating.setReleaseDate(releaseDate);
            }
            //getting versions of ps
            try {
                int i = 1;
                do {
                    String psVersion = doc.getElementsByClass("playable-on__button-set").get(0).childNode(i).childNode(0).toString();
                    gameForUpdating.getDeviceTypes().add(DeviceType.of(psVersion));
                    i += 2;
                } while (true);

            } catch (IllegalArgumentException | IndexOutOfBoundsException ex) {
                logger.debug("Exception in parsing ps versions. Probably they are out.");
                logger.debug(ex.getMessage());
            }

            //getting genres
            try {
                int i = 0;
                do {
                    String stringGenre = doc.getElementsByClass("tech-specs__menu-items").get(i).childNode(0).toString();
                    gameForUpdating.getGenres().add(Genre.of(stringGenre));
                    i++;
                } while (true);

            } catch (IllegalArgumentException | IndexOutOfBoundsException ex) {
                logger.debug("Exception in parsing genres. Probably they are out.");
                logger.debug(ex.getMessage());
            }

            gameService.updateGamePatch(gameForUpdating, gameId);
        } catch (Exception e) {
            logger.info("Exception while parsing data from html");
        }

    }


    private List<Game> getListOfGamesFromHtml(Document doc) {

        Predicate<String> name = s -> s.contains("name");
        Predicate<String> category = s -> s.contains("category");
        Predicate<String> priceCurrency = s -> s.contains("priceCurrency\"");
        Predicate<String> price = s -> s.contains("price\"");
        Predicate<String> sku = s -> s.contains("sku\"");

        Element headline = doc.select("script").attr("type", "application/ld+json").get(0);
        String elementsByAttribute = headline.childNode(0).toString();

        List<String> allFields = Arrays.asList(elementsByAttribute.split(","));

        List<String> sortedList = allFields.stream()
                .filter(name.or(category).or(priceCurrency).or(price).or(sku))
                .collect(Collectors.toList());

        List<Game> games = new ArrayList<>();
        // TODO: Replace if cases
        for (int i = 0; i < sortedList.size(); i++) {
            if (sortedList.get(i).contains("name")) {
                String gameName = sortedList.get(i);
                if (sortedList.get(i + 1).contains("category")) {
                    String gameCategory = sortedList.get(i + 1);
                    if (sortedList.get(i + 2).contains("priceCurrency")) {
                        String gamePriceCurrency = sortedList.get(i + 2);
                        if (sortedList.get(i + 3).contains("price")) {
                            String gamePrice = sortedList.get(i + 3);
                            if (sortedList.get(i + 4).contains("sku")) {
                                String gameSku = sortedList.get(i + 4);
                                Price pr = new Price(subString(gamePrice), getCurrencyFromString(gamePriceCurrency));
                                Game createdGame = new Game(subString(gameName), pr, subString(gameSku), getGameCategory(gameCategory));
                                games.add(createdGame);
                            }
                        }
                    }
                }
            }
            i = i + 4;
        }


        // TODO: Implement PS Plus discount price
        // Pay attention to "price-display__price__label". It may contain smth like "Сэкономьте еще 5% благодаря".
        int amountOfGamesWithPSPlusDiscount = doc.getElementsByClass(psPlusDiscountHtmlClassName).size();
        HashMap<String, Integer> psPlusPriceList = new HashMap<>();
        if (amountOfGamesWithPSPlusDiscount > 0) {
            for (int i = 0; i < amountOfGamesWithPSPlusDiscount; i++) {
                try {
                    String psPlusStringPrice = doc.getElementsByClass(psPlusDiscountHtmlClassName).get(i).childNode(0).toString();
                    int convertedPsPlusPrice = (int) Math.round(Double.parseDouble(psPlusStringPrice.substring(1, 7)));
                    String gameUrl = doc.getElementsByClass(psPlusDiscountHtmlClassName).get(i).parentNode().parentNode().attributes().get("href");
                    String separatedGameUrl = gameUrl.split("/", 4)[3];

                    psPlusPriceList.put(separatedGameUrl, convertedPsPlusPrice);
                } catch (Exception ex) {
                    logger.info("Exception while converting");
                }
            }
        }

        return games;
    }

    private Category getGameCategory(String gameCategory) {
        String catVal = gameCategory.replaceAll("\"", "");
        String resultCategory = catVal.substring(9);

        return Category.of(resultCategory);
    }

    private String subString(String s) {
        if (s.contains("sku")) {
            return s.substring(7, s.length() - 1);
        } else if (s.contains("price")) {
            return s.substring(8, s.length() - 2);
        } else if (s.contains("Премьера")) {
            return s.substring(9);
        } else {
            return s.substring(8, s.length() - 1);
        }
    }

    private Currency getCurrencyFromString(String cur) {
        String curVal = cur.replaceAll("\"", "");
        String result = curVal.substring(14);
        switch (result) {
            case "UAH":
                return Currency.UAH;
            default:
                throw new IllegalArgumentException("Unrecognized price currency.");
        }
    }

//    @Scheduled(fixedDelay = 6000000)
//    public void scheduledTask() throws IOException {
//
//        String allGames = "grid/STORE-MSF75508-FULLGAMES/";
//
//        for (int page = 1; page < 205; page++) {
//            logger.info("Get all prices form page: " + page);
//            getDataFromURL(allGames + page);
//        }
//    }

    @Scheduled(fixedDelay = 60000)
    public void debugScheduledTask() throws IOException {

        logger.info("Get all prices.");
        getDataFromUrlWithJsoup("grid/STORE-MSF75508-FULLGAMES/5");
        logger.info("Getting of all prices is done.");
    }

    @Scheduled(fixedDelay = 60000)
    public void gettingDetailedInfoAboutGames() {
        logger.info("Starting procedure of getting detailed info about games.");
        List<String> urls;
        urls = gameRepository.urlsOfNotUpdatedGames();

        WebDriverManager.getInstance(DriverManagerType.CHROME).setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-extensions");
        options.addArguments("no-sandbox");
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, 10);

        try {
            for (String url : urls) {
                String address = BASE_URL + "product/" + url;

                driver.get(address);
                try {
                    wait.until(ExpectedConditions.numberOfElementsToBe(By.className("provider-info__list-item"), 3));
                } catch (TimeoutException ex) {
                    logger.info("Fail attempt to get 3 releaseDate elements. Probably just rate removed.");
                }
                Document doc = Jsoup.parse(driver.getPageSource());

                String gameId = gameRepository.getGameIdByUrl(url);
                getDataFromUrlWithSelenium(doc, gameId);
            }
        } catch (Exception ex) {
            logger.info("Error while getting Document.");
        } finally {
            driver.close();
            logger.info("Procedure of getting detailed info about games is finished.");
        }
    }
}