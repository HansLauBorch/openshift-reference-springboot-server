package no.skatteetaten.aurora.openshift.reference.springboot;

import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationConfig {

    @Bean
    public RestTemplate restTemplate(
        @Value("${keepalive.host:localhost:8080}") String host,
        RestTemplateBuilder builder,
        CloseableHttpClient httpClient
    ) {
        return builder.requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient)).rootUri(host)
            .build();
    }

}
