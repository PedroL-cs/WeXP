package com.wexp;

import com.sun.net.httpserver.HttpServer;
import com.wexp.feature.category.ICategoryRepository;
import com.wexp.feature.genre.IGenreRepository;
import com.wexp.feature.image.ImageService;
import com.wexp.shared.client.steam.SteamIntegrationException;
import com.wexp.shared.client.steam.SteamRequestExecutor;
import com.wexp.shared.client.steam.api.SteamApiClient;
import com.wexp.shared.client.steam.store.SteamStoreClient;
import com.wexp.shared.config.client.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.wexp.shared.client.steam.SteamIntegrationException.Reason.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SteamHttpResilienceTests {
    private HttpServer server;
    private ExecutorService serverThreads;
    private SteamClientProperties properties;
    private SteamStoreClient store;
    private SteamApiClient api;
    private SteamRequestExecutor requests;
    private ICategoryRepository categories;
    private ImageService images;
    private final AtomicInteger storeCalls = new AtomicInteger();
    private final AtomicInteger status = new AtomicInteger(200);
    private final AtomicInteger delayMillis = new AtomicInteger();
    private final AtomicInteger bodyDelayMillis = new AtomicInteger();
    private final AtomicReference<String> body = new AtomicReference<>("{\"10\":{\"success\":true,\"data\":{\"name\":\"Game\"}}}");
    private final AtomicReference<String> retryAfter = new AtomicReference<>();

    @BeforeEach
    void setup() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        serverThreads = Executors.newCachedThreadPool();
        server.setExecutor(serverThreads);
        server.createContext("/api/appdetails", exchange -> {
            storeCalls.incrementAndGet();
            try {
                Thread.sleep(delayMillis.get());
                byte[] bytes = body.get().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                if (retryAfter.get() != null) exchange.getResponseHeaders().set("Retry-After", retryAfter.get());
                exchange.sendResponseHeaders(status.get(), bytes.length);
                Thread.sleep(bodyDelayMillis.get());
                exchange.getResponseBody().write(bytes);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            } finally {
                exchange.close();
            }
        });
        server.createContext("/ISteamUserStats/GetSchemaForGame/v2", exchange -> {
            byte[] bytes = "{\"game\":{}}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        properties = new SteamClientProperties();
        properties.setReadTimeout(Duration.ofSeconds(2));
        properties.setCircuitFailureThreshold(2);
        properties.setCircuitOpenDuration(Duration.ofMillis(80));
        requests = new SteamRequestExecutor(properties);
        initializeClients();
    }

    private void initializeClients() {
        String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        var factory = new SteamHttpConfig().steamRequestFactory(properties);
        SteamApiClientConfig apiConfig = new SteamApiClientConfig();
        ReflectionTestUtils.setField(apiConfig, "steamApiBaseUrl", baseUrl);
        ReflectionTestUtils.setField(apiConfig, "steamApiKey", "secret-must-not-leak");
        api = new SteamApiClient(apiConfig.steamApi(factory), requests);
        SteamStoreClientConfig storeConfig = new SteamStoreClientConfig();
        ReflectionTestUtils.setField(storeConfig, "steamStoreBaseUrl", baseUrl);
        categories = mock(ICategoryRepository.class);
        images = mock(ImageService.class);
        store = new SteamStoreClient(storeConfig.steamStore(factory), api, requests, images,
                categories, mock(IGenreRepository.class));
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
        serverThreads.shutdownNow();
    }

    @Test
    void shouldApplyRealResponseTimeout() {
        properties.setReadTimeout(Duration.ofMillis(150));
        initializeClients();
        delayMillis.set(600);

        SteamIntegrationException error = assertThrows(SteamIntegrationException.class, () -> store.fetchGameDetails(10L));

        assertEquals(TIMEOUT, error.getReason());
        verifyNoInteractions(categories, images);
    }

    @Test
    void shouldClassifyServiceFailureWithoutExposingUpstreamBody() {
        status.set(503);
        body.set("secret-must-not-leak");

        SteamIntegrationException error = assertThrows(SteamIntegrationException.class, () -> store.fetchGameDetails(10L));

        assertEquals(UNAVAILABLE, error.getReason());
        assertFalse(error.toString().contains("secret-must-not-leak"));
        assertEquals(1, storeCalls.get());
    }

    @Test
    void shouldTimeOutWhileReadingResponseBody() {
        properties.setReadTimeout(Duration.ofMillis(150));
        initializeClients();
        bodyDelayMillis.set(600);

        assertEquals(TIMEOUT, assertThrows(SteamIntegrationException.class,
                () -> store.fetchGameDetails(10L)).getReason());
    }

    @Test
    void shouldNotTreatMissingUpstreamEndpointAsMissingGame() {
        status.set(404);
        assertEquals(REJECTED, assertThrows(SteamIntegrationException.class,
                () -> store.fetchGameDetails(10L)).getReason());
    }

    @Test
    void shouldBlockRepeatedCallsAndRecoverAfterCircuitWait() throws Exception {
        status.set(503);
        assertThrows(SteamIntegrationException.class, () -> store.fetchGameDetails(10L));
        assertThrows(SteamIntegrationException.class, () -> store.fetchGameDetails(10L));
        SteamIntegrationException blocked = assertThrows(SteamIntegrationException.class, () -> store.fetchGameDetails(10L));
        assertEquals(CIRCUIT_OPEN, blocked.getReason());
        assertEquals(2, storeCalls.get());

        // The API has a separate circuit and remains available.
        assertNotNull(api.getSchemaForGame(10L));
        Thread.sleep(120);
        status.set(200);
        assertTrue(store.fetchGameDetails(10L).isPresent());
        assertEquals(3, storeCalls.get());
    }

    @Test
    void shouldHonorRetryAfterSecondsWithoutRepeatingRequest() {
        status.set(429);
        retryAfter.set("60");
        SteamIntegrationException first = assertThrows(SteamIntegrationException.class, () -> store.fetchGameDetails(10L));
        SteamIntegrationException second = assertThrows(SteamIntegrationException.class, () -> store.fetchGameDetails(10L));

        assertEquals(RATE_LIMITED, first.getReason());
        assertEquals(Duration.ofSeconds(60), first.getRetryAfter());
        assertEquals(RATE_LIMITED, second.getReason());
        assertEquals(1, storeCalls.get());
    }

    @Test
    void shouldHonorRetryAfterHttpDate() {
        status.set(429);
        retryAfter.set(ZonedDateTime.now(java.time.ZoneOffset.UTC).plusSeconds(60)
                .format(DateTimeFormatter.RFC_1123_DATE_TIME));

        SteamIntegrationException error = assertThrows(SteamIntegrationException.class, () -> store.fetchGameDetails(10L));

        assertEquals(RATE_LIMITED, error.getReason());
        assertTrue(error.getRetryAfter().compareTo(Duration.ofSeconds(55)) > 0);
    }

    @Test
    void shouldDistinguishMissingGameFromMalformedResponse() {
        body.set("{\"10\":{\"success\":false}}");
        assertTrue(store.fetchGameDetails(10L).isEmpty());

        body.set("{\"10\":{\"data\":{\"name\":\"Game\"}}}");
        assertEquals(INVALID_RESPONSE, assertThrows(SteamIntegrationException.class,
                () -> store.fetchGameDetails(10L)).getReason());
        verifyNoInteractions(categories, images);
    }

    @Test
    void shouldClassifyInvalidJson() {
        body.set("this-is-not-json");
        assertEquals(INVALID_RESPONSE, assertThrows(SteamIntegrationException.class,
                () -> store.fetchGameDetails(10L)).getReason());
    }

    @Test
    void shouldNotOpenCircuitForRejectedCredentials() {
        status.set(403);
        for (int i = 0; i < 3; i++) {
            assertEquals(REJECTED, assertThrows(SteamIntegrationException.class,
                    () -> store.fetchGameDetails(10L)).getReason());
        }
        assertEquals(3, storeCalls.get());
    }
}
