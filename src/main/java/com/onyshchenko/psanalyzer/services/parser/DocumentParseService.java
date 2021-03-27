package com.onyshchenko.psanalyzer.services.parser;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.onyshchenko.psanalyzer.model.Category;
import com.onyshchenko.psanalyzer.model.Currency;
import com.onyshchenko.psanalyzer.model.DeviceType;
import com.onyshchenko.psanalyzer.model.Game;
import com.onyshchenko.psanalyzer.model.Genre;
import com.onyshchenko.psanalyzer.model.Price;
import net.minidev.json.JSONArray;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.jsoup.nodes.Document;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DocumentParseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentParseService.class);

    private static final String DATA_QA = "data-qa";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("d.M.yyyy");
    private static final String CATEGORY_GRID_FORMATTER = "$.props.apolloState.CategoryGrid:%s:ru-ua:%s.products";

    private static final String DETAILED_INFO_FORMATTER = "$.props.apolloState.['Product:%s:ru-ua']";
    private static final String PRICE_INFO_FORMATTER = "$.props.apolloState.['$Product:%s:ru-ua.price']";

    private static final String PAGE_INFO_FORMATTER = "$.props.apolloState.['$CategoryGrid:%s:ru-ua:%s.pageInfo']";

    public Game getDetailedGameInfoFromDocument(Document document) {
        LOGGER.info("Parsing detailed game info from document.");
        Game gameForUpdating = new Game();
        boolean exceptionCaptured = false;
        try {
            Element element = document.getElementsByClass("psw-grid-x psw-fill-x psw-l-space-y-m psw-grid-margin-x psw-m-y-0").get(0);

            String publisher = extractPublisherFromGameElement(element);
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
            LOGGER.info("Exception while parsing data from html.", e);
        }

        gameForUpdating.setErrorWhenFilling(exceptionCaptured);
        LOGGER.debug("Collected data for updating game: \n{}", gameForUpdating.getUpdatedDate());

        return gameForUpdating;
    }

    public List<String> getGamesUrlFromDocument(DocumentContext context, String urlCategory, String size) {

        try {
            LOGGER.info("Starting extracting urls from Document Context.");
            return ((JSONArray) context
                    .read(String.format(CATEGORY_GRID_FORMATTER, urlCategory, size)))
                    .stream()
                    .map(el -> ((Map<?, ?>) el).get("id").toString())
                    .map(s -> s.split(":")).map(s -> s[1])
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("Error in getGamesUrlFromDocument().", e);
            return Collections.emptyList();
        }
    }

    public List<Game> getInitialInfoAboutGamesFromDocumentContext(DocumentContext context, List<String> urls) {

        LOGGER.info("Starting parsing of Document Context with list of games.");

        List<Game> games = new ArrayList<>();
        for (String gameUrl : urls) {
            extractGameFromContextAndAddToList(games, context, gameUrl);
        }

        return games;
    }

    private void extractGameFromContextAndAddToList(List<Game> games, DocumentContext context, String gameUrl) {

        try {
            LinkedHashMap<?, ?> gameInfo = context.read(String.format(DETAILED_INFO_FORMATTER, gameUrl));

            String url = JsonPath.parse(gameInfo).read("id");
            String name = JsonPath.parse(gameInfo).read("name");
            String gameClassification = JsonPath.parse(gameInfo).read("localizedStoreDisplayClassification");

            Game game = new Game();
            game.setUrl(url);
            game.setName(name);

            List<String> platforms = ((JSONArray) ((Map<?, ?>) JsonPath.parse(gameInfo)
                    .read("platforms")).get("json")).stream()
                    .map(Object::toString).collect(Collectors.toList());
            Set<DeviceType> deviceTypes = platforms.stream()
                    .map(DeviceType::of).collect(Collectors.toSet());
            game.setDeviceTypes(deviceTypes);

            game.setCategory(Category.ofRuUaName(gameClassification));

            DocumentContext priceContext = JsonPath.parse((Map<?, ?>) context
                    .read(String.format(PRICE_INFO_FORMATTER, gameUrl)));

            game.setPrice(getPriceBasedOnContext(priceContext));

            boolean isExclusive = priceContext.read("isExclusive");
            game.setExclusive(isExclusive);

            fulfillGameInfoWithSubscriptionsInfo(game, priceContext);

            games.add(game);
        } catch (Exception ex) {
            LOGGER.error("Exception occurred in method extractGameFromContextAndAddToList().", ex);
        }
    }

    private void fulfillGameInfoWithSubscriptionsInfo(Game game, DocumentContext priceContext) {

        Map<?, ?> subscriptionContext = priceContext.read("upsellServiceBranding");

        if (subscriptionContext == null || ((List<?>) JsonPath.parse(subscriptionContext).read("json")).isEmpty()) {
            return;
        }

        List<String> subscriptions = JsonPath.parse(subscriptionContext).read("json");

        for (String subscriptionCategory : subscriptions) {

            if (subscriptionCategory.equalsIgnoreCase("EA_ACCESS")) {
                game.setEaAccess(true);
            } else if (subscriptionCategory.equalsIgnoreCase("PS_PLUS") && game.getPrice() != null) {
                String psPlusUpsellText = priceContext.read("upsellText").toString();
                if (psPlusUpsellText.contains("Сэкономьте еще ")) {
                    String percentageValue = psPlusUpsellText.replace("Сэкономьте еще ", "")
                            .replace("\u00A0%", "");
                    int discountPercent = Integer.parseInt(percentageValue);

                    Price currentPrice = game.getPrice();

                    game.getPrice().setCurrentPsPlusPrice(currentPrice.getCurrentPrice() * (100 - discountPercent) / 100);
                }
            }

        }
    }

    private Price getPriceBasedOnContext(DocumentContext priceContext) {

        String basePrice = priceContext.read("basePrice");
        String discountedPrice = priceContext.read("discountedPrice");

        boolean isFreeGame = priceContext.read("isFree");
        if (isFreeGame) {
            return new Price();
        } else if (basePrice.contains("Недоступно") && discountedPrice.contains("Недоступно")) {
            LOGGER.info("Game is not available for purchase.");
            return new Price().inNotAvailable();
        } else {
            int basePriceInt = convertStringPriceValueToInt(basePrice);
            int discountedPriceInt = convertStringPriceValueToInt(discountedPrice);

            return new Price(discountedPriceInt, basePriceInt, Currency.UAH);
        }
    }

    private int convertStringPriceValueToInt(String priceValue) {
        String fluentBasePrice = priceValue.substring(0, priceValue.length() - 4)
                .replace(" ", "");

        return (int) Double.parseDouble(fluentBasePrice);
    }

    public boolean chekIfTheLastPageOfDocument(DocumentContext context, String category, String size) {

        try {
            return (Boolean) ((Map<?, ?>) context.read(String.format(PAGE_INFO_FORMATTER, category, size))).get("isLast");
        } catch (Exception ex) {
            LOGGER.error("Exception on checking on last page.", ex);
            return true;
        }
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
                    genres.add(Genre.ofRuUaName(cutGenre));
                } else {
                    genres.add(Genre.ofRuUaName(genre));
                }

            }
        } catch (Exception ex) {
            LOGGER.warn("Exception while getting information about GENRES from element: \n{}.", element);
        }

        return genres;
    }

    private String extractPublisherFromGameElement(Element element) {
        String publisher = null;
        try {
            publisher = element.getElementsByAttributeValueContaining(DATA_QA, "publisher-value")
                    .get(0).childNode(0).toString().replace("\n", "");
        } catch (Exception ex) {
            LOGGER.warn("Exception while getting information about publisher from element: \n{}.", element);
        }
        return publisher;
    }

    private LocalDate extractReleaseDateFromGameElement(Element element) {
        LocalDate releaseDate = null;
        try {
            String stringReleaseDate = element.getElementsByAttributeValueContaining(DATA_QA, "releaseDate-value")
                    .get(0).childNode(0).toString().replace("\n", "");
            if (!stringReleaseDate.isEmpty()) {
                releaseDate = LocalDate.parse(stringReleaseDate, DATE_TIME_FORMATTER);
            }
        } catch (Exception ex) {
            LOGGER.warn("Exception while getting information about RELEASE DATE from element: \n{}.", element);
        }

        return releaseDate;
    }

    public Game prepareGameBasedOnSingleGameDocument(Document document, String url) {

        List<?> listOfPrices = JsonPath.parse(document.getElementsByClass("pdp-cta")
                .get(0).childNodes().get(0).childNode(0).toString())
                .read("$.cache.Product:" + url + ".webctas");

        String nameOfBasePriceElement = (String) ((Map<?, ?>) listOfPrices.get(0)).get("__ref");

        Map<?, ?> mapFromPriceContext = getPriceOfDistinctElementFromDocument(document, nameOfBasePriceElement);

        DocumentContext context = JsonPath.parse(mapFromPriceContext);

        Price basePrice = getPriceBasedOnContext(context);
        Game game = new Game();
        game.setPrice(basePrice);
        game.setUrl(url);

        if (listOfPrices.size() > 1) {
            String nameOfSubscriptionElement = (String) ((Map<?, ?>) listOfPrices.get(1)).get("__ref");

            Map<?, ?> subscriptionData = getPriceOfDistinctElementFromDocument(document, nameOfSubscriptionElement);

            String upsellText = (String) subscriptionData.get("upsellText");

            if (upsellText.contains("PS Plus")) {
                Integer discPsPlusPrice = (Integer) subscriptionData.get("discountedValue");
                int psPlusPrice = discPsPlusPrice / 100;
                basePrice.setCurrentPsPlusPrice(psPlusPrice);
            } else if (upsellText.contains("EA")) {
                game.setEaAccess(true);
            }
        }

        return game;
    }

    private Map<?, ?> getPriceOfDistinctElementFromDocument(Document document, String elementName) {
        return JsonPath.parse(document.getElementsByClass("pdp-cta")
                .get(0).childNodes().get(0).childNode(0).toString())
                .read("$.cache." + elementName + ".price");
    }
}
