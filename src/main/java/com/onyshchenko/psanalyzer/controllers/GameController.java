package com.onyshchenko.psanalyzer.controllers;

import com.onyshchenko.psanalyzer.dao.GameRepository;
import com.onyshchenko.psanalyzer.interfaces.controllers.GameControllerIntf;
import com.onyshchenko.psanalyzer.model.Game;
import com.onyshchenko.psanalyzer.model.RequestFilters;
import com.onyshchenko.psanalyzer.services.FilteringUtils;
import com.onyshchenko.psanalyzer.services.GameService;
import com.onyshchenko.psanalyzer.services.SearchUtils.GameSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.bind.ValidationException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
public class GameController implements GameControllerIntf {

    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private GameService gameService;
    @Autowired
    private FilteringUtils filteringUtils;

    public Optional<Game> getGame(@PathVariable(value = "id") String id) {
        logger.info("Trying to get game by id from repository.");
        return gameRepository.findById(id);
    }

    @Override
    public Page<Game> getGames(int page, int size, String filter) throws ValidationException {

        logger.info("Trying to get list of games from repository with params: page= " + page + "; size= " + size);
        if (filter != null) {
            Specification<Game> spec = FilteringUtils.getSpecificationFromFilter(filter);

            if (((GameSpecification) spec).criteria.getKey().equals(RequestFilters.USERID)) {
                List<Game> wl = gameService.prepareWishList(((GameSpecification) spec).criteria.getValue());
                return new PageImpl<>(wl, PageRequest.of(page, size), wl.size());
            }
            return gameRepository.findAll(spec, PageRequest.of(page, size));
        }
        return gameRepository.findAll(PageRequest.of(page, size));
    }

    @Override
    public ResponseEntity<Object> createGame(Game game) {
        logger.info("Trying to create game in repository.");
        gameRepository.save(game);
        return new ResponseEntity<>("Game created.", HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Object> updateGame(Game gameDetails) {
        logger.info("Trying to update the game on repository.");

        Game game = gameRepository.findById(gameDetails.getId()).orElseThrow(
                () -> new IllegalArgumentException("Game not found id: " + gameDetails.getId()));

        gameRepository.save(gameDetails);
        return new ResponseEntity<>("Game updated.", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> deleteGame(@PathVariable String id) {
        logger.info("Trying to delete the game from repository.");
        Game game = gameRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException("Game not found id: " + id));
        gameRepository.delete(game);
        return new ResponseEntity<>("Game deleted.", HttpStatus.OK);
    }

}
