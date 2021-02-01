package com.onyshchenko.psanalyzer.services.parser;

import com.onyshchenko.psanalyzer.model.Category;
import com.onyshchenko.psanalyzer.model.Currency;
import com.onyshchenko.psanalyzer.model.DeviceType;
import com.onyshchenko.psanalyzer.model.Game;
import com.onyshchenko.psanalyzer.model.Genre;
import com.onyshchenko.psanalyzer.model.Price;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.jsoup.nodes.Document;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DocumentParseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentParseService.class);

    private static final String DATA_QA = "data-qa";
    private static final String OLD_PRICE_ELEMENT_DATA = "price price--strikethrough psw-m-l-xs";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("d/M/yyyy");

    public Game getDetailedGameInfoFromDocument(Document document) {
        LOGGER.info("Parsing detailed game info from document.");
        Game gameForUpdating = new Game();
        boolean exceptionCaptured = false;
        try {
            Element element = document.getElementsByClass("psw-grid-x psw-fill-x psw-l-space-y-m psw-grid-margin-x psw-m-y-0").get(0);

            String publisher = extractPublisherFromGmeElement(element);
            if (publisher != null && !publisher.isEmpty()) {
                gameForUpdating.setPublisher(publisher);
                gameForUpdating.setSearchPublisher(publisher.toLowerCase());
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

            DeviceType deviceType = extractDeviceTypeFromGameElement(element);
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
            LOGGER.info("Exception while parsing data from html.", e);
        }

        gameForUpdating.setErrorWhenFilling(exceptionCaptured);
        LOGGER.debug("Collected data for updating game: \n{}", gameForUpdating.getUpdatedDate());
        return gameForUpdating;
    }

    public List<Game> getInitialInfoAboutGamesFromDocument(Document doc, Category category) {

        LOGGER.info("Starting parsing of document from site with list of games.");

        Elements listOfGames = doc.getElementsByClass(
                "ems-sdk-product-tile-link");
        if (listOfGames.isEmpty()) {
            LOGGER.error("Collected 0 elements from document.");
        }
        List<Game> games = new ArrayList<>();
        JSONParser parser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
        for (Element gameInfoInHtml : listOfGames) {
            try {

                JSONObject json = (JSONObject) parser.parse(gameInfoInHtml.attributes().get("data-telemetry-meta"));

                String gameName = json.getAsString("name");
                String gameSku = json.getAsString("id");

                Elements checkIfPriceClassExists = gameInfoInHtml.getElementsByClass("price__container");
                if (checkIfPriceClassExists == null || checkIfPriceClassExists.isEmpty()) {
                    LOGGER.warn("Broken info about game collected. Game name: [{}], id: [{}]", gameName, gameSku);
                    continue;
                }

                int currentPrice = getPreviousPriceFromJson(json);
                int previousPrice = getOldPriceFromElementIfExist(gameInfoInHtml.getElementsByClass(OLD_PRICE_ELEMENT_DATA));
                Currency currency = Currency.UAH;
                Price price = preparePriceFromValues(currentPrice, previousPrice, currency);

                Game createdGame = new Game(gameName, price, gameSku);
                createdGame.setCategory(category);
                LOGGER.debug("Adding game [{}] to list of games from document.", gameName);
                games.add(createdGame);
            } catch (ParseException e) {
                LOGGER.error("Parsing error of element: \n{}.", gameInfoInHtml);
            } catch (Exception ex) {
                LOGGER.error("Unknown exception during document parsing.", ex);
            }
        }
        return games;
    }

    private Price preparePriceFromValues(int currentPrice, int previousPrice, Currency currency) {
        if (previousPrice != 0) {
            return new Price(currentPrice, previousPrice, currency);
        } else {
            if (currentPrice == 0) {
                return new Price();
            } else {
                return new Price(currentPrice, currency);
            }
        }
    }

    private int getPreviousPriceFromJson(JSONObject json) {
        String currentGamePriceString = json.getAsString("price");

        if (currentGamePriceString.equalsIgnoreCase("Входит в подписку")
                || currentGamePriceString.equalsIgnoreCase("бесплатно")) {
            //TODO: Add psplus & eagames subscriptions.
            return 0;
        }

        try {
            String priceString = currentGamePriceString.substring(0, currentGamePriceString.length() - 4)
                    .replace(" ", "");
            return (int) Double.parseDouble(priceString);
        } catch (Exception ex) {
            LOGGER.error("Exception during converting current price to int. Json data: \n{}", json);
            return 0;
        }
    }

    private int getOldPriceFromElementIfExist(Elements oldPriceElementsList) {

        if (oldPriceElementsList.isEmpty()) {
            return 0;
        }
        try {
            String previousPrice = oldPriceElementsList.get(0).childNode(0).toString();
            String fluentPrice = previousPrice.substring(1, previousPrice.length() - 4)
                    .replace(" ", "");
            return (int) Double.parseDouble(fluentPrice);
        } catch (Exception ex) {
            LOGGER.info("Exception in converting of old price from element to int. Element data: \n{}", oldPriceElementsList);
            return 0;
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
                    genres.add(Genre.of(cutGenre));
                }
            }
        } catch (Exception ex) {
            LOGGER.info("Exception while getting information about GENRES from element: \n{}.", element);
        }

        return genres;
    }

    private DeviceType extractDeviceTypeFromGameElement(Element element) {
        DeviceType deviceType = null;
        try {
            String platform = element.getElementsByAttributeValueContaining(DATA_QA, "platform-value")
                    .get(0).childNode(0).toString().replace("\n", "");
            deviceType = DeviceType.of(platform);
        } catch (Exception ex) {
            LOGGER.warn("Exception while getting DEVICE TYPE for element: \n{}.", element);
        }
        return deviceType;
    }


    private String extractPublisherFromGmeElement(Element element) {
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
}
