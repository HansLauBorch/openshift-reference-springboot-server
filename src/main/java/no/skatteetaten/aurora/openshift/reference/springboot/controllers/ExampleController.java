package no.skatteetaten.aurora.openshift.reference.springboot.controllers;

import static no.skatteetaten.aurora.AuroraMetrics.StatusValue.CRITICAL;
import static no.skatteetaten.aurora.AuroraMetrics.StatusValue.OK;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import no.skatteetaten.aurora.AuroraMetrics;
import no.skatteetaten.aurora.openshift.reference.springboot.controllers.dto.S3FileContentRequest;
import no.skatteetaten.aurora.openshift.reference.springboot.controllers.dto.S3FileContentResponse;
import no.skatteetaten.aurora.openshift.reference.springboot.service.S3Service;

/*
 * An example controller that shows how to do a REST call and how to do an operation with a operations metrics
 * There should be a metric called http_client_requests http_server_requests and operations
 */
@RestController
public class ExampleController {

    private final Logger logger = LoggerFactory.getLogger(ExampleController.class);
    private static final String SOMETIMES = "sometimes";
    private static final int SECOND = 1000;
    private RestTemplate restTemplate;
    private AuroraMetrics metrics;
    private S3Service storageService;

    public ExampleController(RestTemplate restTemplate, AuroraMetrics metrics, S3Service storageService) {
        this.storageService = storageService;
        this.restTemplate = restTemplate;
        this.metrics = metrics;
    }

    @GetMapping("/api/example/ip")
    public Map<String, Object> ip() {

        JsonNode forEntity = restTemplate.getForObject("http://httpbin.org/ip", JsonNode.class);
        Map<String, Object> response = new HashMap<>();
        response.put("ip", forEntity.get("origin").textValue());
        logger.info("Server ip={}", response.get("ip"));

        return response;
    }

    @GetMapping("/api/example/sometimes")
    public Map<String, Object> example() {
        return metrics.withMetrics(SOMETIMES, () -> {
            boolean wasSuccessful = performOperationThatMayFail();
            if (wasSuccessful) {
                metrics.status(SOMETIMES, OK);
                Map<String, Object> response = new HashMap<>();
                response.put("result", "Sometimes I succeed");
                return response;
            } else {
                metrics.status(SOMETIMES, CRITICAL);
                throw new RuntimeException("Sometimes I fail");
            }
        });
    }

    @PostMapping("/api/example/s3")
    public S3FileContentResponse uploadFile(@RequestBody S3FileContentRequest request) {
        storageService
            .putFileContent(request.getFileName(), request.getContent(), request.isUseDefaultBucketObjectArea());
        String storedFileContent =
            storageService.getFileContent(request.getFileName(), request.isUseDefaultBucketObjectArea());
        return new S3FileContentResponse(storedFileContent);
    }

    protected boolean performOperationThatMayFail() {

        long sleepTime = (long) (Math.random() * SECOND);

        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Sleep interupted", e);
        }

        return sleepTime % 2 == 0;
    }
}

