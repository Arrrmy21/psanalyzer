package com.onyshchenko.psanalyzer.dao;

import com.onyshchenko.psanalyzer.model.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, String> {

    Page<Game> findAll(Specification<Game> spec, Pageable page);
}