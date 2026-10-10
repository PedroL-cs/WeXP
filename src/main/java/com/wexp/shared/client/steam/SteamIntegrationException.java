package com.wexp.shared.client.steam;

import lombok.Getter;

import java.time.Duration;

@Getter
public class SteamIntegrationException extends RuntimeException {
    public enum Reason { TIMEOUT, UNAVAILABLE, RATE_LIMITED, INVALID_RESPONSE, REJECTED, NOT_FOUND, CIRCUIT_OPEN }

    private final Reason reason;
    private final Duration retryAfter;

    public SteamIntegrationException(Reason reason) {
        this(reason, null);
    }

    public SteamIntegrationException(Reason reason, Duration retryAfter) {
        // Do not include upstream URLs, response bodies or API credentials in the exception message.
        super("Falha na integração Steam: " + reason);
        this.reason = reason;
        this.retryAfter = retryAfter;
    }

    public boolean isRetryable() {
        return reason == Reason.TIMEOUT || reason == Reason.UNAVAILABLE;
    }
}
