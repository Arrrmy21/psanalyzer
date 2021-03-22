package com.onyshchenko.psanalyzer.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "prices")
public class Price {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(name = "id")
    private String id;
    @Column(name = "currentPrice")
    private int currentPrice;
    @Column(name = "previousPrice")
    private int previousPrice;
    @Column(name = "highestPrice")
    private int highestPrice;
    @Column(name = "highestPriceDate")
    private LocalDate highestPriceDate;
    @Column(name = "lowestPriceDate")
    private LocalDate lowestPriceDate;
    @Column(name = "lowestPrice")
    private int lowestPrice;
    @Column(name = "highestDiscount")
    private int highestDiscount;
    @Column(name = "currentDiscount")
    private int currentDiscount;
    @Column(name = "currentPercentageDiscount")
    private int currentPercentageDiscount;
    @Column(name = "highestPercentageDiscount")
    private int highestPercentageDiscount;
    @Enumerated(EnumType.STRING)
    private Currency currency;
    @Column(name = "price_changed")
    private boolean priceChanged = false;
    @Column(name = "psplusPrice")
    private int currentPsPlusPrice;
    @Column(name = "isAvailable")
    private boolean isAvailable = true;

    public Price() {
        this.currentPrice = 0;
        this.previousPrice = 0;
        this.highestPrice = 0;
        this.highestPriceDate = LocalDate.now();
        this.lowestPriceDate = LocalDate.now();
        this.lowestPrice = currentPrice;
        this.highestDiscount = 0;
        this.currentDiscount = 0;
        this.currentPercentageDiscount = 0;
        this.highestPercentageDiscount = 0;
        this.currentPsPlusPrice = 0;
    }

    public Price(int currentPrice, Currency currency) {
        this();
        this.currentPrice = currentPrice;
        this.lowestPrice = currentPrice;
        this.currency = currency;
        this.highestPrice = currentPrice;
        this.previousPrice = currentPrice;
        this.currentPsPlusPrice = currentPrice;
    }

    public Price(@NotNull int currentPrice, int previousPrice, Currency currency) {
        this();
        this.currentPrice = currentPrice;
        this.previousPrice = previousPrice;
        this.highestPrice = previousPrice;
        this.lowestPrice = currentPrice;
        this.currentDiscount = previousPrice - currentPrice;
        this.highestDiscount = currentDiscount;
        calculatePercentageDiscount();
        this.highestPercentageDiscount = currentPercentageDiscount;
        this.currentPsPlusPrice = currentPrice;
        this.currency = currency;
    }

    public Price inNotAvailable() {
        this.isAvailable = false;
        return this;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getHighestPercentageDiscount() {
        return highestPercentageDiscount;
    }

    public void setHighestPercentageDiscount(int highestPercentageDiscount) {
        this.highestPercentageDiscount = highestPercentageDiscount;
    }

    public int getCurrentPercentageDiscount() {
        return currentPercentageDiscount;
    }

    public void setCurrentPercentageDiscount(int currentPercentageDiscount) {
        this.currentPercentageDiscount = currentPercentageDiscount;
    }

    public int getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(int currentPrice) {
        this.currentPrice = currentPrice;
    }

    public int getPreviousPrice() {
        return previousPrice;
    }

    public void setPreviousPrice(int previousPrice) {
        this.previousPrice = previousPrice;
    }

    public int getHighestPrice() {
        return highestPrice;
    }

    public void setHighestPrice(int highestPrice) {
        this.highestPrice = highestPrice;
    }

    public LocalDate getHighestPriceDate() {
        return highestPriceDate;
    }

    public void setHighestPriceDate(LocalDate highestPriceDate) {
        this.highestPriceDate = highestPriceDate;
    }

    public LocalDate getLowestPriceDate() {
        return lowestPriceDate;
    }

    public void setLowestPriceDate(LocalDate lowestPriceDate) {
        this.lowestPriceDate = lowestPriceDate;
    }

    public int getLowestPrice() {
        return lowestPrice;
    }

    public void setLowestPrice(int lowestPrice) {
        this.lowestPrice = lowestPrice;
    }

    public int getHighestDiscount() {
        return highestDiscount;
    }

    public void setHighestDiscount(int highestDiscount) {
        this.highestDiscount = highestDiscount;
    }

    public int getCurrentDiscount() {
        return currentDiscount;
    }

    public void setCurrentDiscount(int currentDiscount) {
        this.currentDiscount = currentDiscount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public boolean hasDiscount() {
        return currentDiscount != 0;
    }

    public void calculatePercentageDiscount() {
        this.currentPercentageDiscount = (100 - getCurrentPrice() * 100 / getPreviousPrice());
    }

    public boolean isPriceChanged() {
        return priceChanged;
    }

    public void setPriceChanged(boolean priceChanged) {
        this.priceChanged = priceChanged;
    }

    public int getCurrentPsPlusPrice() {
        return currentPsPlusPrice;
    }

    public void setCurrentPsPlusPrice(int currentPsPlusPrice) {
        this.currentPsPlusPrice = currentPsPlusPrice;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public void calculateDiscount() {
        this.currentDiscount = getPreviousPrice() - getCurrentPrice();
    }
}