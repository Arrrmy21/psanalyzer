package com.onyshchenko.psanalyzer.services;

import com.onyshchenko.psanalyzer.model.Game;
import com.onyshchenko.psanalyzer.model.Price;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class HtmlHookService {

    @Autowired
    private GameService gameService;

    private static final Logger logger = LoggerFactory.getLogger(HtmlHookService.class);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final String BASE_URL = "https://store.playstation.com/ru-ua/";

    public void getDataFromURL(String url) throws IOException {

        String address = BASE_URL + url;
        Document doc = Jsoup.connect(address).get();
        logger.info(doc.title());

        List<Game> games = getListOfGames(doc);

        gameService.checkList(games);
        logger.info(games.toString());

    }

    private List<Game> getListOfGames(Document doc) {

        Predicate<String> name = s -> s.contains("name");
        Predicate<String> price = s -> s.contains("price\"");
        Predicate<String> sku = s -> s.contains("sku\"");

        Element headline = doc.select("script").attr("type", "application/ld+json").get(0);

        String elementsByAttribute = headline.childNode(0).toString();

        List<String> allFields = Arrays.asList(elementsByAttribute.split(","));

        List<String> sortedList = allFields.stream()
                .filter(name.or(price).or(sku))
                .collect(Collectors.toList());

        List<Game> games = new ArrayList<>();
        for (int i = 0; i < sortedList.size(); i++) {
            if (sortedList.get(i).contains("name")) {
                String gameName = sortedList.get(i);
                if (sortedList.get(i + 1).contains("price")) {
                    String gamePrice = sortedList.get(i + 1);
                    if (sortedList.get(i + 2).contains("sku")) {
                        String gameSku = sortedList.get(i + 2);
                        games.add(new Game(subString(gameName), new Price(subString(gamePrice)), subString(gameSku)));
                    }
                }
            }
            i = i + 2;
        }

        return games;
    }

    private String subString(String s) {
        if (s.contains("sku")) {
            return s.substring(7, s.length() - 1);
        } else if (s.contains("price")) {
            return s.substring(8, s.length() - 2);
        } else {
            return s.substring(8, s.length() - 1);
        }
    }

    @Scheduled(fixedDelay = 6000000)
    public void scheduledTask() throws IOException {

        String allGames = "grid/STORE-MSF75508-FULLGAMES/";

        for (int page = 1; page < 205; page++) {
            logger.info("Get all prices form page: " + page);
            getDataFromURL(allGames + page);
        }
    }

//    @Scheduled(fixedDelay = 60000)
//    public void debugScheduledTask() throws IOException, InterruptedException {
//
//        logger.info("Get exact price", dateTimeFormatter.format(LocalDateTime.now()));
//        getDataFromURL("product/EP0822-CUSA08403_00-DEADAGEPS4SIEE00");
//        logger.info("Get all prices", dateTimeFormatter.format(LocalDateTime.now()));
//        getDataFromURL("home/games");
//    }

}
