package com.onyshchenko.psanalyzer.model;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.OneToOne;
import javax.persistence.JoinColumn;
import javax.persistence.CascadeType;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_id")
    private Long id;

    @NotNull
    @Column(name = "game_name")
    private String name;

    @NotNull
    @Column(name = "game_search_name")
    private String searchName;

    @NotNull
    @Column(name = "game_url", unique = true)
    private String url;

    @Enumerated(EnumType.STRING)
    private Category category;

    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "prices_id")
    private Price price;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Genre> genres;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<DeviceType> deviceTypes;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "detailed_info")
    private boolean detailedInfoFilledIn = false;

    @Column(name = "error_filling")
    private Boolean errorWhenFilling;

    @Column(name = "game_publisher")
    private String publisher;

    @Column(name = "game_search_publisher")
    private String searchPublisher;

    @ManyToMany(mappedBy = "wishList")
    private Collection<User> users;

    private Boolean isInWl;

    public Game() {
    }

    public Game(String name, Price price, String url) {
        this.name = name;
        this.searchName = name.toLowerCase();
        this.url = url;
        this.price = price;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSearchName() {
        return searchName;
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

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getSearchPublisher() {
        return searchPublisher;
    }

    public void setSearchPublisher(String searchPublisher) {
        this.searchPublisher = searchPublisher;
    }

    public Boolean isErrorWhenFilling() {
        return errorWhenFilling;
    }

    public void setErrorWhenFilling(Boolean errorWhenFilling) {
        this.errorWhenFilling = errorWhenFilling;
    }

    public Boolean getInWl() {
        return isInWl;
    }

    public void setInWl(Boolean inWl) {
        isInWl = inWl;
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

