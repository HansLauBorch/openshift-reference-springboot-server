package no.skatteetaten.aurora.openshift.reference.springboot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;

import io.findify.s3mock.S3Mock;
import no.skatteetaten.aurora.openshift.reference.springboot.ApplicationConfig;
import no.skatteetaten.aurora.openshift.reference.springboot.service.dto.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

@RestClientTest(components = { ApplicationConfig.class, S3Service.class })
class S3ServiceTest {

    @Autowired
    private S3Configuration s3Config;

    @Autowired
    private S3Service s3Service;

    private final S3Mock s3Mock = new S3Mock.Builder().withInMemoryBackend().withPort(9000).build();

    @BeforeEach
    void setUp() {
        s3Mock.start();
    }

    @AfterEach
    void tearDown() {
        s3Mock.shutdown();
    }

    @Test
    @DisplayName("Is able to store and retrieve object from s3 with default bucket")
    void storeAndRetrieveObjectFromDefaultBucket() {
        s3Config.getS3Client().createBucket(CreateBucketRequest.builder().bucket("default").build());
        String expectedFileContent = "my awesome test file";

        s3Service.putFileContent("myFile.txt", expectedFileContent, true);
        String fileContent = s3Service.getFileContent("myFile.txt", true);

        assertEquals(expectedFileContent, fileContent);
    }

    @Test
    @DisplayName("Verify is able to store and retrieve object from s3 with other bucket")
    void storeAndRetrieveObjectFromOtherBucket() {
        s3Config.getS3Client().createBucket(CreateBucketRequest.builder().bucket("default").build());
        String expectedFileContent = "my awesome test file2";

        s3Service.putFileContent("myFile.txt", expectedFileContent, false);
        String fileContent = s3Service.getFileContent("myFile.txt", false);

        assertEquals(expectedFileContent, fileContent);
    }

}