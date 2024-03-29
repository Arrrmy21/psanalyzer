package com.onyshchenko.psanalyzer.services.mapper;

import com.onyshchenko.psanalyzer.dao.GameRepository;
import com.onyshchenko.psanalyzer.model.Currency;
import com.onyshchenko.psanalyzer.model.Game;
import com.onyshchenko.psanalyzer.model.Price;
import com.onyshchenko.psanalyzer.services.GameService;
import com.onyshchenko.psanalyzer.services.PriceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @InjectMocks
    private GameService gameService;
    @Mock
    private GameRepository gameRepository;
    @Mock
    private PriceService priceService;
    @Captor
    private ArgumentCaptor<Game> gameArgumentCaptor;

    private static final long GAME_ID = 123;
    private static final String GAME_NAME = "Game-Name-1";
    private static final String GAME_URL = "game-url-123";

    @Test
    final void checkGamePriceUpdatedTest() {

        Game gameFromDb = getGame(getPriceFromDb());
        assertEquals(150, gameFromDb.getPrice().getCurrentPrice());

        when(gameRepository.findByUrl(GAME_URL)).thenReturn(Optional.of(gameFromDb));
        when(gameRepository.save(any())).thenReturn(gameFromDb);

        doAnswer(invocation -> {
            Price arg0 = invocation.getArgument(0);
            Price arg1 = invocation.getArgument(1);

            assertEquals(getNewPriceFromSite().getCurrentPrice(), arg0.getCurrentPrice());
            assertEquals(getPriceFromDb().getCurrentPrice(), arg1.getCurrentPrice());
            arg1.setCurrentPrice(arg0.getCurrentPrice());
            return null;
        }).when(priceService).updatePriceComparingWithExisting(any(), any());

        Game newGameFromSite = getGame(getNewPriceFromSite());

        gameService.compareCollectedListOfGamesToExisted(Collections.singletonList(newGameFromSite));
        Mockito.verify(gameRepository).save(gameArgumentCaptor.capture());

        Game gameForUpdate = gameArgumentCaptor.getValue();
        assertEquals(100, gameForUpdate.getPrice().getCurrentPrice());
        assertEquals(150, gameForUpdate.getPrice().getPreviousPrice());

    }

    private Game getGame(Price price) {
        Game game = new Game();
        game.setId(GAME_ID);
        game.setName(GAME_NAME);
        game.setUrl(GAME_URL);
        game.setPrice(price);

        return game;
    }

    private Price getNewPriceFromSite() {
        return new Price(100, 150, Currency.UAH);
    }

    private Price getPriceFromDb() {
        return new Price(150, Currency.UAH);
    }

}
