package com.onyshchenko.psanalyzer.services.mapper;

import com.onyshchenko.psanalyzer.model.Currency;
import com.onyshchenko.psanalyzer.model.Price;
import com.onyshchenko.psanalyzer.services.PriceService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PriceServiceTest {

    PriceService priceService = new PriceService();

    private static final int CURRENT_SITE_PRICE = 100;
    private static final int PREVIOUS_SITE_PRICE = 150;
    private static final int CURRENT_DB_PRICE = 150;


    @Test
    final void checkPriceUpdatedTest() {

        Price priceFromSite = getNewPriceFromSite();
        Price priceFromDb = getPriceFromDb();

        assertEquals(0, priceFromDb.getCurrentDiscount());
        assertEquals(0, priceFromDb.getCurrentPercentageDiscount());
        assertEquals(CURRENT_DB_PRICE, priceFromDb.getCurrentPrice());

        priceService.updatePriceComparingWithExisting(priceFromSite, priceFromDb);

        assertEquals(CURRENT_SITE_PRICE, priceFromDb.getCurrentPrice());
        assertEquals(PREVIOUS_SITE_PRICE, priceFromDb.getPreviousPrice());
        assertEquals(50, priceFromDb.getCurrentDiscount());
        assertEquals(34, priceFromDb.getCurrentPercentageDiscount());
    }

    @Test
    final void checkPriceUpdatedWithDiscountTest() {

        Price priceFromSite = getNewPriceFromSite();
        Price priceFromDb = getPriceFromDbWithDiscount();

        assertEquals(30, priceFromDb.getCurrentDiscount());
        assertEquals(20, priceFromDb.getCurrentPercentageDiscount());
        assertEquals(120, priceFromDb.getCurrentPrice());
        assertEquals(120, priceFromDb.getLowestPrice());
        assertEquals(150, priceFromDb.getHighestPrice());

        priceService.updatePriceComparingWithExisting(priceFromSite, priceFromDb);

        assertEquals(CURRENT_SITE_PRICE, priceFromDb.getCurrentPrice());
        assertEquals(PREVIOUS_SITE_PRICE, priceFromDb.getPreviousPrice());
        assertEquals(50, priceFromDb.getCurrentDiscount());
        assertEquals(34, priceFromDb.getCurrentPercentageDiscount());
        assertEquals(100, priceFromDb.getLowestPrice());
    }

    @Test
    void checkCorrectDiscountPercentageCalculation() {

        int oldPrice = CURRENT_DB_PRICE;
        int newPrice = CURRENT_SITE_PRICE;
        int currentDiscount = 50;
        int currentPercentDiscount = 34;

        int calculatedDisount = priceService.evaluateDiscount(oldPrice, newPrice);
        assertEquals(currentDiscount, calculatedDisount);
        int calculatedPercentDiscount = priceService.evaluatePercentageDiscount(oldPrice, newPrice);
        assertEquals(currentPercentDiscount, calculatedPercentDiscount);
    }

    @Test
    void checkBasePriceChanged() {
        Price priceFromSite = getNewPriceFromSite();
        priceFromSite.setPreviousPrice(130);
        priceFromSite.setHighestPrice(130);

        Price priceFromDb = getPriceFromDbWithDiscount();
        assertEquals(150, priceFromDb.getPreviousPrice());
        assertFalse(priceFromDb.isPriceChanged());

        priceService.updatePriceComparingWithExisting(priceFromSite, priceFromDb);
        assertEquals(130, priceFromDb.getPreviousPrice());
        assertTrue(priceFromDb.isPriceChanged());

    }

    @Test
    void verifyDiscountIsNullAfterSaleIsOver() {

        Price priceFromDb = getPriceFromDbWithDiscount();
        Price newPriceFromSite = getNewPriceFromSiteWithoutDiscount();

        priceService.updatePriceComparingWithExisting(newPriceFromSite, priceFromDb);

        assertEquals(PREVIOUS_SITE_PRICE, priceFromDb.getCurrentPrice());
        assertEquals(PREVIOUS_SITE_PRICE, priceFromDb.getPreviousPrice());
        assertEquals(0, priceFromDb.getCurrentDiscount());
        assertEquals(0, priceFromDb.getCurrentPercentageDiscount());
        assertEquals(120, priceFromDb.getLowestPrice());
    }

    private Price getNewPriceFromSite() {
        return new Price(CURRENT_SITE_PRICE, PREVIOUS_SITE_PRICE, Currency.UAH);
    }

    private Price getNewPriceFromSiteWithoutDiscount() {
        return new Price(PREVIOUS_SITE_PRICE, Currency.UAH);
    }

    private Price getPriceFromDb() {
        return new Price(CURRENT_DB_PRICE, Currency.UAH);
    }

    private Price getPriceFromDbWithDiscount() {

        return new Price(120, CURRENT_DB_PRICE, Currency.UAH);
    }

}
