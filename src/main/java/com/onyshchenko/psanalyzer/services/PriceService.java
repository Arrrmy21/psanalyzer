package com.onyshchenko.psanalyzer.services;

import com.onyshchenko.psanalyzer.model.Price;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PriceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PriceService.class);

    public Price updatePriceComparingWithExisting(Price gameFromSite, Price gameFromDB) {
        Price newPrice = new Price();

        newPrice.setCurrentPrice(gameFromSite.getCurrentPrice());
        newPrice.setHighestPrice(gameFromDB.getHighestPrice());
        newPrice.setHighestPriceDate(gameFromDB.getHighestPriceDate());

        int oldPrice = gameFromDB.getCurrentPrice();
        int actualPrice = gameFromSite.getCurrentPrice();
        int lowestPrice = gameFromDB.getLowestPrice();
        newPrice.setId(gameFromDB.getId());

        if (actualPrice < lowestPrice) {
            LOGGER.info("Reached the lowest price.");
            newPrice.setLowestPrice(actualPrice);
            newPrice.setLowestPriceDate(gameFromSite.getLowestPriceDate());

            int currentDiscount = evaluateDiscount(oldPrice, actualPrice);
            int currentPercentageDiscount = evaluatePercentageDiscount(oldPrice, actualPrice);
            newPrice.setCurrentDiscount(currentDiscount);
            newPrice.setCurrentPercentageDiscount(currentPercentageDiscount);

            if (currentDiscount > gameFromDB.getHighestDiscount()) {
                LOGGER.info("Reached the highest discount.");
                newPrice.setHighestDiscount(currentDiscount);
                newPrice.setHighestPercentageDiscount(currentPercentageDiscount);
            }
        } else {
            newPrice.setLowestPrice(gameFromDB.getLowestPrice());
            newPrice.setLowestPriceDate(gameFromDB.getLowestPriceDate());
            newPrice.setCurrentDiscount(0);
            newPrice.setCurrentPercentageDiscount(0);
            newPrice.setHighestDiscount(gameFromDB.getHighestDiscount());
            newPrice.setHighestPercentageDiscount(gameFromDB.getHighestPercentageDiscount());
        }
        return newPrice;
    }

    private int evaluateDiscount(int oldPrice, int newPrice) {
        return oldPrice - newPrice;
    }

    private int evaluatePercentageDiscount(int oldPrice, int newPrice) {
        return (100 - newPrice * 100 / oldPrice);
    }
}
