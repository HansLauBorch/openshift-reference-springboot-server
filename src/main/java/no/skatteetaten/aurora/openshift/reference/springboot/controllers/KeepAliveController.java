package no.skatteetaten.aurora.openshift.reference.springboot.controllers;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

/*
 * An example controller that shows how to do a REST call and how to do an operation with a operations metrics
 * There should be a metric called http_client_requests http_server_requests and operations
 */
@RestController()
public class KeepAliveController {

    private final String podName;
    private final String auroraVersion;
    private final RestTemplate restTemplate;
    private final Logger logger = LoggerFactory.getLogger(KeepAliveController.class);
    private final int number;
    private final int wait;

    public KeepAliveController(
        @Value("${pod.name:localhost}") String podName,
        @Value("${aurora.version:local-dev}") String auroraVersion,
        @Value("${keepalive.wait:200}") int wait,
        @Value("${keepalive.number:1000}") int number,
        RestTemplate restTemplate) {

        this.wait = wait;
        this.number = number;
        this.restTemplate = restTemplate;
        this.podName = podName;
        this.auroraVersion = auroraVersion;
    }

    @GetMapping("/keepalive/server")
    public Map<String, Object> serveTest() {
        logger.info("Received request");
        return Map.of(
            "version", auroraVersion,
            "name", podName
        );
    }

    @GetMapping("/keepalive/client")
    public void clientTest() {
        for (int i = 0; i < number; i++) {
            StopWatch watch = new StopWatch();
            watch.start();
            try {
                Thread.sleep(wait);
                Map<String, String> uriVars = Map.of();
                ResponseEntity<JsonNode> entity =
                    restTemplate.getForEntity("/keepalive/server", JsonNode.class, uriVars);
                watch.stop();
                long totalTimeMillis = watch.getTotalTimeMillis();
                String clientName = "";
                if (entity.getBody() != null && entity.getBody().get("name") != null) {
                    clientName = entity.getBody().get("name").asText();
                }
                List<String> strings = entity.getHeaders().get("Keep-Alive");
                logger.info("response={} server={} client={} time={}ms keepalive={}", entity.getStatusCodeValue(),
                    podName,
                    clientName, totalTimeMillis, strings);
            } catch (Exception e) {
                watch.stop();
                long totalTimeMillis = watch.getTotalTimeMillis();
                logger.warn("Feil skjedde etter tid=" + totalTimeMillis, e);
            }
        }
        logger.info("Done {} requests", number);
    }
}
