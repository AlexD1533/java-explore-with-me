package client;

import dto.EndpointHitDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

public class StatsClient {

    private final RestTemplate rest;
    private final String serverUrl;


    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        this.serverUrl = serverUrl;
        this.rest = builder
                .rootUri(serverUrl)
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(2))
                .build();

    }

    public void saveHit(EndpointHitDto hitDto) {
        rest.postForLocation("/hit", hitDto);
    }




}
