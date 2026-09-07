package com.wexp.shared.config.client;

import com.wexp.shared.client.steam.api.SteamApi;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Configuration
public class SteamApiClientConfig {

    @Value("${steam.api.base-url}")
    private String steamApiBaseUrl;

    @Value("${steam.api.key}")
    private String steamApiKey;

    @Bean
    public SteamApi steamApi() {
        RestClient restClient = RestClient.builder()
                .baseUrl(steamApiBaseUrl)
                .requestInterceptor(addSteamApiKey())
                .build();

        RestClientAdapter restClientAdapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(restClientAdapter).build();

        return factory.createClient(SteamApi.class);
    }

    private ClientHttpRequestInterceptor addSteamApiKey() {
        return (request, body, execution) -> {
            URI uri = UriComponentsBuilder
                    .fromUri(request.getURI())
                    .queryParam("key", steamApiKey)
                    .build(true)
                    .toUri();

            HttpRequest modifiedRequest = new HttpRequestWrapper(request) {
                @Override
                @NullMarked
                public URI getURI() {
                    return uri;
                }
            };

            return execution.execute(modifiedRequest, body);
        };
    }
}
