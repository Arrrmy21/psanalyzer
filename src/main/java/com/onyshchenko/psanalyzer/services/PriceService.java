package com.onyshchenko.psanalyzer.services;

import com.onyshchenko.psanalyzer.model.Price;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PriceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PriceService.class);

    public void updatePriceComparingWithExisting(Price gameFromSite, Price gameToBeUpdatedOnDB) {

        if (!gameFromSite.isAvailable()) {
            gameToBeUpdatedOnDB.setAvailable(false);
            return;
        }

        int actualPrice = gameFromSite.getCurrentPrice();
        int previousSitePrice = gameFromSite.getPreviousPrice();

        int gameDbOldPrice = gameToBeUpdatedOnDB.getPreviousPrice();
        if (previousSitePrice != gameDbOldPrice) {
            LOGGER.info("Base price of game changed");
            gameToBeUpdatedOnDB.setPriceChanged(true);
        }

        int lowestPrice = gameToBeUpdatedOnDB.getLowestPrice();
        int highestPrice = gameToBeUpdatedOnDB.getHighestPrice();

        int currentDiscount = evaluateDiscount(previousSitePrice, actualPrice);
        int currentPercentageDiscount = evaluatePercentageDiscount(previousSitePrice, actualPrice);
        gameToBeUpdatedOnDB.setCurrentDiscount(currentDiscount);
        gameToBeUpdatedOnDB.setCurrentPercentageDiscount(currentPercentageDiscount);

        if (actualPrice < lowestPrice) {
            LOGGER.info("Reached the lowest price.");
            gameToBeUpdatedOnDB.setLowestPrice(actualPrice);
            gameToBeUpdatedOnDB.setLowestPriceDate(LocalDate.now());

            if (currentDiscount > gameToBeUpdatedOnDB.getHighestDiscount()) {
                LOGGER.info("Reached the highest discount.");
                gameToBeUpdatedOnDB.setHighestDiscount(currentDiscount);
                gameToBeUpdatedOnDB.setHighestPercentageDiscount(currentPercentageDiscount);
            }
        } else if (actualPrice > highestPrice) {
            LOGGER.info("Reached the highest price. WTF?");

            gameToBeUpdatedOnDB.setHighestPrice(actualPrice);
            gameToBeUpdatedOnDB.setHighestPriceDate(LocalDate.now());
        }

        gameToBeUpdatedOnDB.setCurrentPrice(actualPrice);
        if (actualPrice == previousSitePrice) {
            gameToBeUpdatedOnDB.setCurrentDiscount(0);
            gameToBeUpdatedOnDB.setCurrentPercentageDiscount(0);
        }
        gameToBeUpdatedOnDB.setPreviousPrice(previousSitePrice);
        gameToBeUpdatedOnDB.setCurrentPsPlusPrice(gameFromSite.getCurrentPsPlusPrice());

        if (!gameToBeUpdatedOnDB.isAvailable()) {
            gameToBeUpdatedOnDB.setAvailable(true);
            gameToBeUpdatedOnDB.setLowestPrice(gameToBeUpdatedOnDB.getCurrentPrice());
            gameToBeUpdatedOnDB.setHighestPrice(gameToBeUpdatedOnDB.getCurrentPrice());
            gameToBeUpdatedOnDB.calculateDiscount();
            gameToBeUpdatedOnDB.calculatePercentageDiscount();
        }
    }

    public int evaluateDiscount(int oldPrice, int newPrice) {
        int newDiscount = oldPrice - newPrice;
        return Math.max(newDiscount, 0);
    }

    public int evaluatePercentageDiscount(int oldPrice, int newPrice) {
        if (oldPrice == 0 || oldPrice > newPrice) {
            return 0;
        }
        return (100 - newPrice * 100 / oldPrice);
    }
}
