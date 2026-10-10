package com.wexp.shared.config.client;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Validated
@ConfigurationProperties("steam.client")
public class SteamClientProperties {
    @NotNull private Duration connectTimeout = Duration.ofSeconds(3);
    @NotNull private Duration readTimeout = Duration.ofSeconds(10);
    @Min(1) private int circuitFailureThreshold = 5;
    @NotNull private Duration circuitOpenDuration = Duration.ofSeconds(30);
    @NotNull private Duration refreshInterval = Duration.ofDays(7);
    @Min(1) private int refreshMaxAttempts = 2;
    @NotNull private Duration refreshRetryDelay = Duration.ofMillis(250);

    @AssertTrue(message = "Os intervalos Steam devem ser positivos; o atraso de retentativa pode ser zero")
    public boolean isTimingValid() {
        return positive(connectTimeout) && positive(readTimeout) && positive(circuitOpenDuration)
                && positive(refreshInterval) && refreshRetryDelay != null && !refreshRetryDelay.isNegative();
    }

    private boolean positive(Duration duration) {
        return duration != null && !duration.isZero() && !duration.isNegative();
    }
}
