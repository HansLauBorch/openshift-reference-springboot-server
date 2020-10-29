package no.skatteetaten.aurora.openshift.reference.springboot.controllers;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
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

    private Logger logger = LoggerFactory.getLogger(KeepAliveController.class);
    private final String podName;
    private final String auroraVersion;
    private final RestTemplate restTemplate;

    public KeepAliveController(
        @Value("${pod.name:localhost}") String podName,
        @Value("${aurora.version:local-dev}") String auroraVersion,
        RestTemplate restTemplate) {

        this.restTemplate = restTemplate;
        this.podName = podName;
        this.auroraVersion = auroraVersion;
    }

    @GetMapping("/keepalive/server")
    public Map<String, Object> serveTest(HttpServletRequest request) {
        return Map.of(
            "version", auroraVersion,
            "name", podName
        );
    }

    @GetMapping("/keepalive/client")
    public JsonNode clientTest() {
        Map<String, String> uriVars = Map.of();
        ResponseEntity<JsonNode> entity = restTemplate.getForEntity("/keepalive/server", JsonNode.class, uriVars);
         logger.info(entity.toString());
        return entity.getBody();
    }
}
