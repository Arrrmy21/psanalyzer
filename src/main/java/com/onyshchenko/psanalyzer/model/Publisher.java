package com.onyshchenko.psanalyzer.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "publishers")
@Getter
@Setter
@NoArgsConstructor
public class Publisher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "publisher_id")
    private Long id;

    @Column(name = "name")
    String name;

    @Column(name = "surname")
    String surname;

    @Column(name = "search_name")
    String searchName;
    String surname;

    @Column(name = "search_Path")
    String searchPath;

    @Column(name = "search_Id")
    String searchId;

    public Publisher(String name) {
        this.name = name;
        this.searchName = name.toLowerCase();
    }

    public Publisher(Long id, String name, String surname, String searchName) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.searchName = searchName;
    }
}
