package com.wexp.shared.config.client;

import com.wexp.shared.client.steam.store.SteamStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class SteamStoreClientConfig {

    @Value("${steam.store.base-url}")
    private String steamStoreBaseUrl;

    @Bean
    public SteamStore steamStore() {
        RestClient restClient = RestClient.builder()
                .baseUrl(steamStoreBaseUrl)
                .build();

        RestClientAdapter restClientAdapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(restClientAdapter).build();

        return factory.createClient(SteamStore.class);
    }
}
