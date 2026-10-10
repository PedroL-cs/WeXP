package com.wexp;

import com.wexp.feature.game.GameController;
import com.wexp.feature.game.GameService;
import com.wexp.shared.client.steam.SteamIntegrationException;
import com.wexp.shared.exception.GlobalExceptionHandler;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SteamFailureApiTests {
    @ParameterizedTest
    @CsvSource({"TIMEOUT,504,20", "UNAVAILABLE,503,19", "CIRCUIT_OPEN,503,19",
            "INVALID_RESPONSE,502,21", "REJECTED,502,21", "NOT_FOUND,404,2", "RATE_LIMITED,503,19"})
    void shouldReturnSpecificApiError(SteamIntegrationException.Reason reason, int status, int code) throws Exception {
        GameService games = mock(GameService.class);
        when(games.viewSteamGameDetails(10L)).thenThrow(new SteamIntegrationException(reason, Duration.ofSeconds(60)));
        var mvc = MockMvcBuilders.standaloneSetup(new GameController(games, null))
                .setControllerAdvice(new GlobalExceptionHandler()).build();

        mvc.perform(get("/api/v1/games/steam/10"))
                .andExpect(status().is(status))
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(header().string("Retry-After", "60"));
    }
}
