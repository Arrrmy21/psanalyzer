package com.onyshchenko.psanalyzer.services;

import com.onyshchenko.psanalyzer.dao.GameRepository;
import com.onyshchenko.psanalyzer.dao.UserRepository;
import com.onyshchenko.psanalyzer.model.Game;
import com.onyshchenko.psanalyzer.model.User;
import com.onyshchenko.psanalyzer.services.mapper.GameMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class GameService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameService.class);

    @Autowired
    private PriceService priceService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private GameMapper gameMapper;

    public Optional<Game> findGameIfAlreadyExists(String id) {
        return gameRepository.findById(id);
    }

    public void checkList(List<Game> games) {

        LOGGER.info("Checking list of games.");

        if (games.isEmpty()) {
            LOGGER.info("List of games is empty.");
            return;
        }

        for (Game game : games) {
            LOGGER.info("Checking game record for name: [{}].", game.getName());
            Optional<Game> gameFromDb = findGameIfAlreadyExists(game.getId());
            if (!gameFromDb.isPresent()) {
                LOGGER.info("Creating game record.");
                gameRepository.save(game);
            } else {
                if (game.getPrice().getCurrentPrice() != gameFromDb.get().getPrice().getCurrentPrice()) {
                    LOGGER.info("Checking game price.");

                    priceService.updatePriceComparingWithExisting(game.getPrice(), gameFromDb.get().getPrice());

                    gameRepository.save(gameFromDb.get());
                } else LOGGER.info("Actual game already exists in DB.");
            }
        }
    }

    public Page<Game> prepareWishList(Pageable pageable, long userId) {

        Optional<User> user = userRepository.findById(userId);
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;

        List<String> gameList = new ArrayList<>();
        List<Game> games = new ArrayList<>();
        if (user.isPresent()) {
            gameList = new ArrayList<>(user.get().getWishList());

            for (String id : gameList) {
                Optional<Game> game = gameRepository.findById(id);
                if (game.isPresent()) {
                    games.add(game.get());
                } else {
                    LOGGER.info("Game [{}] in user's wishlist doesn't exist", id);
                }
            }

            int toIndex = Math.min(startItem + pageSize, gameList.size());
            games = games.subList(startItem, toIndex);

        }
        return new PageImpl<>(games, PageRequest.of(currentPage, pageSize), gameList.size());
    }

    public void updateGamePatch(Game updatedData, String gameId) {

        Optional<Game> gameFromDb = gameRepository.findById(gameId);

        Game gameDb;
        if (gameFromDb.isPresent()) {
            gameDb = gameFromDb.get();
            gameMapper.updateGameData(updatedData, gameDb);

            gameRepository.save(gameDb);
        }

    }

    public List<String> getUrlsOfNotUpdatedGames() {

        return gameRepository.urlsOfNotUpdatedGames();
    }

    public String getGameIdByUrl(String url) {
        return gameRepository.getGameIdByUrl(url);
    }
}