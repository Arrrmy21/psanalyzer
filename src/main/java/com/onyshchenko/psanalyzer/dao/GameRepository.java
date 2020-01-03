package com.onyshchenko.psanalyzer.dao;

import com.onyshchenko.psanalyzer.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, String> {
}