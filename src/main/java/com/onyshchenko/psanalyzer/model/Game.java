package com.onyshchenko.psanalyzer.model;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.OneToOne;
import javax.persistence.JoinColumn;
import javax.persistence.CascadeType;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "games")
public class Game {

    @Id
    @NotNull
    @Column(name = "id")
    private String id;

    @NotNull
    @Column(name = "game_name")
    private String name;

    @Column(name = "game_url")
    private String url;

    @Enumerated(EnumType.STRING)
    private Category category;

    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "prices_id")
    private Price price;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Genre> genres;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<DeviceType> deviceTypes;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "detailed_info")
    private boolean detailedInfoFilledIn = false;

    public Game() {
    }

    public Game(String name, Price price, String url, Category category) {
        this.id = String.valueOf(name.hashCode());
        this.name = name;
        this.url = url;
        this.price = price;
        this.category = category;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Set<Genre> getGenres() {
        return genres == null ? genres = new HashSet<>() : genres;
    }

    public void setGenres(Set<Genre> genres) {
        this.genres = genres;
    }

    public Set<DeviceType> getDeviceTypes() {
        return deviceTypes == null ? deviceTypes = new HashSet<>() : deviceTypes;
    }

    public void setDeviceTypes(Set<DeviceType> deviceTypes) {
        this.deviceTypes = deviceTypes;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public boolean isDetailedInfoFilledIn() {
        return detailedInfoFilledIn;
    }

    public void setDetailedInfoFilledIn(boolean detailedInfoFilledIn) {
        this.detailedInfoFilledIn = detailedInfoFilledIn;
    }

    @Override
    public String toString() {
        return "Game{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", price='" + price + '\'' +
                '}';
    }
}

