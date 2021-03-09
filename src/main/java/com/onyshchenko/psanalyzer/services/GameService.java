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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public Optional<Game> findGameByName(String name) {
        return gameRepository.findByName(name);
    }

    public void compareCollectedListOfGamesToExisted(List<Game> games) {

        if (games.isEmpty()) {
            LOGGER.warn("List of games is empty.");
            return;
        }
        for (Game game : games) {
            LOGGER.info("Checking game record for name: [{}].", game.getName());
            Optional<Game> gameFromDb = findGameByName(game.getName());

            if (!gameFromDb.isPresent()) {
                LOGGER.info("Saving game to DB with name: [{}].", game.getName());
                saveGameRecordIntoDb(game);
            } else {
                if (currentGamePriceDiffersThenStored(game, gameFromDb.get())) {
                    LOGGER.info("Price of game in DB differs from site. Updating price for game: [{}]", game.getName());
                    priceService.updatePriceComparingWithExisting(game.getPrice(), gameFromDb.get().getPrice());
                    saveGameRecordIntoDb(gameFromDb.get());
                }
                LOGGER.info("Actual game info is already in database.");
            }
        }
    }

    private boolean currentGamePriceDiffersThenStored(Game game, Game gameFromDb) {
        return game.getPrice().getCurrentPrice() != gameFromDb.getPrice().getCurrentPrice() ||
                game.getPrice().getCurrentPsPlusPrice() != gameFromDb.getPrice().getCurrentPsPlusPrice();
    }

    public void saveGameRecordIntoDb(Game game) {
        try {
            Game savedGame = gameRepository.save(game);
            LOGGER.info("Game [{}] saved to DB with id: [{}].", game.getName(), savedGame.getId());
            savePriceChangingHistory(savedGame);
        } catch (Exception ex) {
            LOGGER.error("Exception during saving game to database", ex);
        }
    }

    private void savePriceChangingHistory(Game game) {
        try {
            gameRepository.saveHistory(game.getId(), game.getPrice().getCurrentPrice(),
                    game.getPrice().getCurrentPsPlusPrice(), LocalDate.now());
            LOGGER.info("Saved price history for game [{}]. Game id: [{}]", game.getName(), game.getId());
        } catch (Exception ex) {
            LOGGER.info("Exception during saving price history", ex);
        }
    }

    public Page<Game> getListOfGames(PageRequest pageRequest, String filter) throws ValidationException {

        if (filter == null) {
            return gameRepository.findAll(pageRequest);
        }
        Specification<Game> spec = FilteringUtils.getSpecificationFromFilter(filter);

        if (((GameSpecification) spec).getCriteria().getKey().equals(RequestFilters.USERID)) {
            String sp = String.valueOf(((GameSpecification) spec).getCriteria().getValue());
            long userId = Long.parseLong(sp);

            return prepareWishList(pageRequest, userId);
        }
        return gameRepository.findAll(spec, pageRequest);

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
        if (!user.isPresent()) {
            LOGGER.warn("User with id [{}] doesn't exist.", userId);
            return Page.empty();
        }

        Set<Long> allGameIdsInWishlistOfUser = user.get().getWishList();
        List<Long> paginatedGameIdsFromWishlist = correlateListOfGamesToPagination(allGameIdsInWishlistOfUser, pageable);

        List<Game> games = new ArrayList<>();
        for (Long id : paginatedGameIdsFromWishlist) {
            Optional<Game> game = gameRepository.findById(id);
            if (game.isPresent()) {
                games.add(game.get());
            } else {
                LOGGER.info("Game [{}] in user's wishlist doesn't exist", id);
            }
        }
        LOGGER.info("Collected [{}] games of wishlist for user: [{}]", games.size(), userId);
        return new PageImpl<>(games, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()),
                paginatedGameIdsFromWishlist.size());
    }

    private List<Long> correlateListOfGamesToPagination(Set<Long> setOfGames, Pageable pageable) {

        LOGGER.debug("Preparing paginated list of user's wishlist");
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;

        int toIndex = Math.min(startItem + pageSize, setOfGames.size());
        List<Long> listOfGames = new ArrayList<>(setOfGames);
        return listOfGames.subList(startItem, toIndex);
    }

    public void updateGamePatch(Game updatedData, long gameId) {
        Optional<Game> gameFromDb = gameRepository.findById(gameId);
        if (gameFromDb.isPresent()) {
            Game gameDb = gameFromDb.get();
            gameMapper.updateGameData(updatedData, gameDb);

            gameRepository.save(gameDb);
            LOGGER.info("Game [{}] updated with Patch method.", gameId);
        }
    }

    public List<String> getUrlsOfNotUpdatedGames() {
        return gameRepository.urlsOfNotUpdatedGames();
    }

    public List<String> getUrlsOfAllGames() {
        return gameRepository.urlsOfAllGames();
    }

    public long getGameIdByUrl(String url) {
        return gameRepository.getGameIdByUrl(url);
    }

    public Optional<Game> getGame(long gameId) {
        return gameRepository.findById(gameId);
    }

    public Optional<Game> getPersonalizedGame(long gameId, long userId) {
        Optional<Game> game = gameRepository.findById(gameId);
        Optional<User> user = userRepository.findById(userId);
        LOGGER.info("Getting personalized game game data [{}] for user: [{}].", gameId, userId);
        if (!game.isPresent() || !user.isPresent()) {
            return game;
        }

        for (Long id : user.get().getWishList()) {
            if (id.equals(game.get().getId())) {
                game.get().setInWl(true);
                break;
            }
        }

        return game;
    }

    public Optional<Game> createGame(Game game) {
        LOGGER.info("Creating game record with name [{}].", game.getName());
        Game createdGame = gameRepository.save(game);

        return Optional.of(createdGame);
    }

    public Optional<Game> getGameByID(long id) {
        return gameRepository.findById(id);
    }

    public ResponseEntity<Object> deleteGame(long gameId) {
        Optional<Game> game = getGameByID(gameId);
        if (game.isPresent()) {
            gameRepository.delete(game.get());
            return new ResponseEntity<>("Game with id [" + gameId + "] deleted ", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Game with id [" + gameId + "] not found ", HttpStatus.NO_CONTENT);
        }
    }

}