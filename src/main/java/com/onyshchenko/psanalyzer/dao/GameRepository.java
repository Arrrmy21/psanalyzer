package com.onyshchenko.psanalyzer.dao;

import com.onyshchenko.psanalyzer.model.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, String> {

    Page<Game> findAll(Specification<Game> spec, Pageable page);

    @Query("select url from Game games where games.detailedInfoFilledIn = false")
    List<String> urlsOfNotUpdatedGames();

    @Query("select id from Game games where games.url = :url")
    String getGameIdByUrl(@Param("url") String url);
}