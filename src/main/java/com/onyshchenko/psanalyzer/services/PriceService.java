package com.onyshchenko.psanalyzer.services;

import com.onyshchenko.psanalyzer.model.Price;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PriceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PriceService.class);

    public void updatePriceComparingWithExisting(Price gameFromSite, Price gameFromDB) {

        int actualPrice = gameFromSite.getCurrentPrice();

        int oldPrice = gameFromDB.getCurrentPrice();
        int lowestPrice = gameFromDB.getLowestPrice();
        int highestPrice = gameFromDB.getHighestPrice();


        if (actualPrice < lowestPrice) {
            LOGGER.info("Reached the lowest price.");
            gameFromDB.setLowestPrice(actualPrice);
            gameFromDB.setLowestPriceDate(LocalDate.now());

            int currentDiscount = evaluateDiscount(oldPrice, actualPrice);
            int currentPercentageDiscount = evaluatePercentageDiscount(oldPrice, actualPrice);
            gameFromDB.setCurrentDiscount(currentDiscount);
            gameFromDB.setCurrentPercentageDiscount(currentPercentageDiscount);

            if (currentDiscount > gameFromDB.getHighestDiscount()) {
                LOGGER.info("Reached the highest discount.");
                gameFromDB.setHighestDiscount(currentDiscount);
                gameFromDB.setHighestPercentageDiscount(currentPercentageDiscount);
            }
        } else if (actualPrice > highestPrice) {
            LOGGER.info("Reached the highest price. WTF?");

            gameFromDB.setHighestPrice(actualPrice);
            gameFromDB.setHighestPriceDate(LocalDate.now());
            gameFromDB.setCurrentDiscount(0);
            gameFromDB.setCurrentPercentageDiscount(0);
        }

        gameFromDB.setCurrentPrice(actualPrice);
    }

    private int evaluateDiscount(int oldPrice, int newPrice) {
        return oldPrice - newPrice;
    }

    private int evaluatePercentageDiscount(int oldPrice, int newPrice) {
        return (100 - newPrice * 100 / oldPrice);
    }
}
