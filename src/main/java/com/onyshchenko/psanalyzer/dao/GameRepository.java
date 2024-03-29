package com.onyshchenko.psanalyzer.dao;

import com.onyshchenko.psanalyzer.model.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    Page<Game> findAll(Specification<Game> spec, Pageable page);

    Optional<Game> findByName(String name);

    Optional<Game> findByNameAndUrl(String name, String url);

    Optional<Game> findByUrl(String url);

    @Query("select url from Game games where games.detailedInfoFilledIn = false")
    List<String> urlsOfNotUpdatedGames();

    @Query("select url from Game games")
    List<String> urlsOfAllGames();

    @Query("select id from Game games where games.url = :url")
    Long getGameIdByUrl(@Param("url") String url);

    @Modifying
    @Transactional
    @Query(value = "insert into Games_history (date_of_change, game_id, game_price, psplus_price, is_available) values (:date, :id, :price, :psPlusPrice, :isAvailable)", nativeQuery = true)
    void saveHistory(@Param("id") long id, @Param("price") int price, @Param("psPlusPrice") int psPlusPrice, @Param("date") LocalDate date, @Param("isAvailable") boolean isAvailable);

}