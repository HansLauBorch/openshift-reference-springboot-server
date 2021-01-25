package no.skatteetaten.aurora.openshift.reference.springboot;

import java.net.http.HttpClient;
import java.time.Duration;

import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import no.skatteetaten.aurora.openshift.reference.springboot.psa.OpenShiftTokenReviewService;

@Configuration
public class ApplicationConfig {

    @Bean
    public RestTemplate restTemplate(
        @Value("${keepalive.host:http://localhost:8080}") String host,
        RestTemplateBuilder builder,
        CloseableHttpClient httpClient
    ) {
        return builder.requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient)).rootUri(host)
            .build();
    }

    @Bean
    public OpenShiftTokenReviewService openShiftTokenReview(
        @Value("${pod.service.account.token.path:/tmp/client-sa}") String serviceTokenPath, // TODO Change to path to app service account, /run/secrets/kubernetes.io/serviceaccount/token
        @Value("${pod.psat.tokenreviewer:https://api.utv04.paas.skead.no/apis/authentication.k8s.io/v1/tokenreviews}") String tokenReviewUrl
    ) {
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();
        return new OpenShiftTokenReviewService( client, serviceTokenPath, tokenReviewUrl);
    }
}
