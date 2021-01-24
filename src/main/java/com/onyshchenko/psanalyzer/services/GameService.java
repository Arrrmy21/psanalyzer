package com.onyshchenko.psanalyzer.services;

import com.onyshchenko.psanalyzer.dao.GameRepository;
import com.onyshchenko.psanalyzer.dao.UserRepository;
import com.onyshchenko.psanalyzer.model.Game;
import com.onyshchenko.psanalyzer.model.RequestFilters;
import com.onyshchenko.psanalyzer.model.User;
import com.onyshchenko.psanalyzer.services.mapper.GameMapper;
import com.onyshchenko.psanalyzer.services.searchutils.GameSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.xml.bind.ValidationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    public Optional<Game> findGameIfAlreadyExists(long id) {
        return gameRepository.findById(id);
    }

    public Optional<Game> findGameByName(String name) {
        return gameRepository.findByName(name);
    }

    public void checkCollectedListOfGamesToExisted(List<Game> games) {

        LOGGER.info("Checking list of games.");

        if (games.isEmpty()) {
            LOGGER.info("List of games is empty.");
            return;
        }

        for (Game game : games) {
            LOGGER.info("Checking game record for name: [{}].", game.getName());
            Optional<Game> gameFromDb = findGameByName(game.getName());
            if (!gameFromDb.isPresent()) {
                LOGGER.info("Creating game record.");
                try {
                    Game savedGame = gameRepository.save(game);
                    gameRepository.saveHistory(savedGame.getId(), game.getPrice().getCurrentPrice(), LocalDate.now());
                } catch (Exception ex) {
                    LOGGER.info("Exception occurred while saving game [{}].", game.getId());
                }
            } else {
                if (game.getPrice().getCurrentPrice() != gameFromDb.get().getPrice().getCurrentPrice()) {
                    LOGGER.info("Game price changed. Collecting and comparing data.");
                    try {
                        gameRepository.saveHistory(game.getId(), game.getPrice().getCurrentPrice(), LocalDate.now());
                        priceService.updatePriceComparingWithExisting(game.getPrice(), gameFromDb.get().getPrice());
                    } catch (Exception e) {
                        LOGGER.error("Exception during updating price.");
                    }

                    gameRepository.save(gameFromDb.get());
                } else LOGGER.info("Actual game already exists in DB.");
            }
        }
    }

    public Page<Game> getListOfGames(PageRequest pageRequest, String filter) throws ValidationException {

        if (filter != null) {
            Specification<Game> spec = FilteringUtils.getSpecificationFromFilter(filter);

            if (((GameSpecification) spec).getCriteria().getKey().equals(RequestFilters.USERID)) {
                String sp = String.valueOf(((GameSpecification) spec).getCriteria().getValue());
                long userId = Long.parseLong(sp);

                return prepareWishList(pageRequest, userId);
            }
            return gameRepository.findAll(spec, pageRequest);
        }
        return gameRepository.findAll(pageRequest);
    }

    public Page<Game> getPersonalizedListOfGames(PageRequest pageRequest, String filter, long userId)
            throws ValidationException {
        Page<Game> listOfGames = getListOfGames(pageRequest, filter);

        Optional<User> user = userRepository.findById(userId);
        Set<Long> usersWishList = new HashSet<>();
        if (user.isPresent()) {
            usersWishList = user.get().getWishList();
        }
        for (Long gameId : usersWishList) {
            listOfGames.get().filter(game -> game.getId().equals(gameId)).forEach(g -> g.setInWl(true));
        }

        return listOfGames;
    }

    public Page<Game> prepareWishList(Pageable pageable, long userId) {

        Optional<User> user = userRepository.findById(userId);
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;

        List<Long> gameList = new ArrayList<>();
        List<Game> games = new ArrayList<>();
        if (user.isPresent()) {
            gameList = new ArrayList<>(user.get().getWishList());

            for (Long id : gameList) {
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

    public void updateGamePatch(Game updatedData, long gameId) {

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

    public long getGameIdByUrl(String url) {
        return gameRepository.getGameIdByUrl(url);
    }

    public Optional<Game> getGame(long gameId) {
        return gameRepository.findById(gameId);
    }

    public Optional<Game> getPersonalizedGame(long gameId, long userId) {
        Optional<Game> game = gameRepository.findById(gameId);

        if (!game.isPresent()) {
            return game;
        }
        Optional<User> user = userRepository.findById(userId);

        Set<Long> usersWishList = new HashSet<>();
        if (user.isPresent()) {
            usersWishList = user.get().getWishList();
        }
        for (Long id : usersWishList) {
            if (id.equals(game.get().getId())) {
                game.get().setInWl(true);
                break;
            }
        }

        return game;
    }
}