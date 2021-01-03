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

        int actualPrice = gameFromSite.getCurrentPrice();
        int previousSitePrice = gameFromSite.getPreviousPrice();

        int gameDbOldPrice = gameToBeUpdatedOnDB.getCurrentPrice();
        if (previousSitePrice != gameDbOldPrice) {
            LOGGER.info("Base price of game changed");
            gameToBeUpdatedOnDB.setPriceChanged(true);
        }

        int lowestPrice = gameToBeUpdatedOnDB.getLowestPrice();
        int highestPrice = gameToBeUpdatedOnDB.getHighestPrice();

        if (actualPrice < lowestPrice) {
            LOGGER.info("Reached the lowest price.");
            gameToBeUpdatedOnDB.setLowestPrice(actualPrice);
            gameToBeUpdatedOnDB.setLowestPriceDate(LocalDate.now());

            int currentDiscount = evaluateDiscount(previousSitePrice, actualPrice);
            int currentPercentageDiscount = evaluatePercentageDiscount(previousSitePrice, actualPrice);
            gameToBeUpdatedOnDB.setCurrentDiscount(currentDiscount);
            gameToBeUpdatedOnDB.setCurrentPercentageDiscount(currentPercentageDiscount);

            if (currentDiscount > gameToBeUpdatedOnDB.getHighestDiscount()) {
                LOGGER.info("Reached the highest discount.");
                gameToBeUpdatedOnDB.setHighestDiscount(currentDiscount);
                gameToBeUpdatedOnDB.setHighestPercentageDiscount(currentPercentageDiscount);
            }
        } else if (actualPrice > highestPrice) {
            LOGGER.info("Reached the highest price. WTF?");

            gameToBeUpdatedOnDB.setHighestPrice(actualPrice);
            gameToBeUpdatedOnDB.setHighestPriceDate(LocalDate.now());
            gameToBeUpdatedOnDB.setCurrentDiscount(0);
            gameToBeUpdatedOnDB.setCurrentPercentageDiscount(0);
        }

        gameToBeUpdatedOnDB.setCurrentPrice(actualPrice);
        gameToBeUpdatedOnDB.setPreviousPrice(previousSitePrice);
    }

    public int evaluateDiscount(int oldPrice, int newPrice) {
        int newDiscount = oldPrice - newPrice;
        return Math.max(newDiscount, 0);
    }

    public int evaluatePercentageDiscount(int oldPrice, int newPrice) {
        if (oldPrice == 0) {
            return 0;
        }
        return (100 - newPrice * 100 / oldPrice);
    }
}
