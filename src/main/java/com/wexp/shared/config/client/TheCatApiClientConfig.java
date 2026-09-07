package com.wexp.shared.config.client;

import com.wexp.shared.client.thecatapi.TheCatApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class TheCatApiClientConfig {

    @Value("${thecatapi.api.base-url}")
    private String theCatApiBaseUrl;

    @Bean
    public TheCatApi theCatApi() {
        RestClient restClient = RestClient.builder()
                .baseUrl(theCatApiBaseUrl)
                .build();

        RestClientAdapter restClientAdapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(restClientAdapter).build();

        return factory.createClient(TheCatApi.class);
    }
}
