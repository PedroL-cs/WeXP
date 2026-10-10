package com.wexp.shared.client.steam;

import com.wexp.shared.config.client.SteamClientProperties;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import static com.wexp.shared.client.steam.SteamIntegrationException.Reason.*;

@Component
public class SteamRequestExecutor {
    public static final String STORE = "steam-store";
    public static final String API = "steam-api";

    private final CircuitBreakerRegistry circuits;
    private final Map<String, Instant> rateLimits = new ConcurrentHashMap<>();
    private final Clock clock = Clock.systemUTC();

    public SteamRequestExecutor(SteamClientProperties properties) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(properties.getCircuitFailureThreshold())
                .minimumNumberOfCalls(properties.getCircuitFailureThreshold())
                .failureRateThreshold(100)
                .waitDurationInOpenState(properties.getCircuitOpenDuration())
                .permittedNumberOfCallsInHalfOpenState(1)
                .ignoreException(error -> error instanceof SteamIntegrationException steam
                        && (steam.getReason() == NOT_FOUND || steam.getReason() == REJECTED))
                .build();
        circuits = CircuitBreakerRegistry.of(config);
    }

    public <T> T execute(String service, Supplier<T> operation) {
        Instant resumeAt = rateLimits.get(service);
        if (resumeAt != null && resumeAt.isAfter(clock.instant())) {
            throw new SteamIntegrationException(RATE_LIMITED, Duration.between(clock.instant(), resumeAt));
        }
        try {
            return circuits.circuitBreaker(service).executeSupplier(() -> {
                try {
                    return operation.get();
                } catch (HttpStatusCodeException exception) {
                    int status = exception.getStatusCode().value();
                    if (status == 429) {
                        Duration wait = retryAfter(exception);
                        rateLimits.merge(service, clock.instant().plus(wait), (first, second) -> first.isAfter(second) ? first : second);
                        throw new SteamIntegrationException(RATE_LIMITED, wait);
                    }
                    // Steam reports a missing game in its JSON payload, not by a missing API endpoint.
                    if (status == 404) throw new SteamIntegrationException(REJECTED);
                    if (status >= 500) throw new SteamIntegrationException(UNAVAILABLE);
                    throw new SteamIntegrationException(REJECTED);
                } catch (ResourceAccessException exception) {
                    if (isTimeout(exception)) throw new SteamIntegrationException(TIMEOUT);
                    throw new SteamIntegrationException(UNAVAILABLE);
                } catch (RestClientException exception) {
                    if (isTimeout(exception)) throw new SteamIntegrationException(TIMEOUT);
                    throw new SteamIntegrationException(INVALID_RESPONSE);
                }
            });
        } catch (CallNotPermittedException exception) {
            throw new SteamIntegrationException(CIRCUIT_OPEN);
        }
    }

    private boolean isTimeout(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof SocketTimeoutException || cause instanceof HttpTimeoutException
                    || cause instanceof TimeoutException) return true;
        }
        return false;
    }

    private Duration retryAfter(HttpStatusCodeException exception) {
        String value = exception.getResponseHeaders() == null ? null : exception.getResponseHeaders().getFirst("Retry-After");
        if (value != null) {
            try {
                Duration wait = Duration.ofSeconds(Math.max(1, Long.parseLong(value.trim())));
                clock.instant().plus(wait);
                return wait;
            } catch (RuntimeException ignored) {
                try {
                    Duration wait = Duration.between(clock.instant(), ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant());
                    if (!wait.isNegative() && !wait.isZero()) return wait;
                } catch (RuntimeException ignoredDate) {
                    // Use a bounded default when the upstream header is invalid.
                }
            }
        }
        return Duration.ofSeconds(30);
    }
}
