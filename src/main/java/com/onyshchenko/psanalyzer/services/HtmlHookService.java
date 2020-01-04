package com.onyshchenko.psanalyzer.services;

import com.onyshchenko.psanalyzer.interfaces.controllers.GameControllerIntf;
import com.onyshchenko.psanalyzer.model.Game;
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
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class HtmlHookService {

    @Autowired
    private GameControllerIntf gameController;

    private static final Logger logger = LoggerFactory.getLogger(HtmlHookService.class);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final String BASE_URL = "https://store.playstation.com/ru-ua/";

    //    public static void main(String[] args) throws IOException {
    public void getPrices(String url) throws IOException {
//        String url = "product/EP0822-CUSA08403_00-DEADAGEPS4SIEE00";
//        String url = "home/games";
        String address = BASE_URL + url;
        Document doc = Jsoup.connect(address).get();
        logger.info(doc.title());

        List<Game> games = getListOfGames(doc);

        games.stream().forEach(g -> gameController.createGame(g));
        System.out.println(games);

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
                        games.add(new Game(subString(gameName), subString(gamePrice), subString(gameSku)));
                    }
                }
            }
            i = i + 2;
        }

        return games;
    }

    private static String subString(String s) {
        if (s.contains("sku")) {
            return s.substring(7, s.length() - 1);
        } else if (s.contains("price")) {
            return s.substring(8, s.length() - 2);
        } else {
            return s.substring(8, s.length() - 1);
        }
    }


    @Scheduled(fixedDelay = 10000)
    public void scheduledTssk() throws IOException, InterruptedException {

        logger.info("get all prices", dateTimeFormatter.format(LocalDateTime.now()));
        getPrices("product/EP0822-CUSA08403_00-DEADAGEPS4SIEE00");
        logger.info("sleep", dateTimeFormatter.format(LocalDateTime.now()));
        Thread.sleep(3000);
        logger.info("get exact price", dateTimeFormatter.format(LocalDateTime.now()));
        getPrices("home/games");
    }
}