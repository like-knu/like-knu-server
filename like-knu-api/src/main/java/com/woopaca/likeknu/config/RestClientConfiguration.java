package com.woopaca.likeknu.config;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class RestClientConfiguration {

    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.build();
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory requestFactory() {
        CloseableHttpClient httpClient = HttpClients.custom()
                .disableRedirectHandling()
                .build();
        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }

    @Bean
    public RestClient restClient(HttpComponentsClientHttpRequestFactory requestFactory) {
        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }
}
