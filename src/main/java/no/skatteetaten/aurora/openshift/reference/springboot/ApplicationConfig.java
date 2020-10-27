package no.skatteetaten.aurora.openshift.reference.springboot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationConfig {

    @Bean
    public RestTemplate restTemplate(
        @Value("${keepalive.host:localhost:8080}") String host,
        RestTemplateBuilder builder) {

        return builder.rootUri(host).build();
    }
}
