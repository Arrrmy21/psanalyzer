package com.onyshchenko.psanalyzer.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
@Getter
@Setter
@NoArgsConstructor
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

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;

    @ManyToMany(mappedBy = "wishList")
    private Collection<User> users;

    private Boolean isInWl;
    private Boolean isExclusive;
    private Boolean isEaAccess = false;


    public Game(String name, Price price, String url) {
        this.name = name;
        this.searchName = name.toLowerCase();
        this.url = url;
        this.price = price;
    }

    public void setName(String name) {
        this.name = name;
        this.searchName = name.toLowerCase();
    }

    public Set<DeviceType> getDeviceTypes() {
        return deviceTypes == null ? deviceTypes = new HashSet<>() : deviceTypes;
    }

    public String getUpdatedDate() {
        return "Game:" +
                "name= " + getName() + "\n" +
                "publisher= " + getPublisher().getName() + "\n" +
                "release date= " + getReleaseDate() + "\n" +
                "device type= " + getDeviceTypes() + "\n" +
                "genres= " + getGenres() + ".";
    }
}

