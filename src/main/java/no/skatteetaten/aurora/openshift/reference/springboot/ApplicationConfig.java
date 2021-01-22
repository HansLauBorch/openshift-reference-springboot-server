package no.skatteetaten.aurora.openshift.reference.springboot;

import java.net.URI;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import no.skatteetaten.aurora.filter.logging.AuroraHeaderFilter;
import no.skatteetaten.aurora.filter.logging.RequestKorrelasjon;
import no.skatteetaten.aurora.openshift.reference.springboot.service.dto.S3Configuration;
import no.skatteetaten.aurora.openshift.reference.springboot.service.dto.S3Properties;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableConfigurationProperties({ S3Properties.class })
public class ApplicationConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .defaultHeader(AuroraHeaderFilter.KORRELASJONS_ID, RequestKorrelasjon.getId())
            .build();
    }

    @Primary
    @Bean
    public S3Configuration defaultS3Client(
        S3Properties s3Properties
    ) {
        return baseS3Configuration(s3Properties, "referanse-java");
    }

    @Qualifier("otherArea")
    @Bean
    public S3Configuration otherS3Client(
        S3Properties s3Properties
    ) {
        return baseS3Configuration(s3Properties, "referanse-java2");
    }

    private S3Configuration baseS3Configuration(S3Properties s3Properties, String objectAreaKey) {
        S3Properties.S3Bucket objectArea = s3Properties.getBuckets().get(objectAreaKey);
        S3Client client = S3Client.builder()
            .region(Region.of(objectArea.getBucketRegion()))
            .credentialsProvider(
                StaticCredentialsProvider
                    .create(AwsBasicCredentials.create(objectArea.getAccessKey(), objectArea.getSecretKey()))
            ).endpointOverride(URI.create(objectArea.getServiceEndpoint()))
            .build();

        return new S3Configuration(client, objectArea);
    }

}
