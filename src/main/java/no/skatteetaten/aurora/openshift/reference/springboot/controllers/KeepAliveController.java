package no.skatteetaten.aurora.openshift.reference.springboot.controllers;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

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

    public KeepAliveController(
        @Value("${pod.name:localhost}") String podName,
        @Value("${aurora.version:local-dev}") String auroraVersion,
        RestTemplate restTemplate) {

        this.restTemplate = restTemplate;
        this.podName = podName;
        this.auroraVersion = auroraVersion;
    }

    @GetMapping("/keepalive/server")
    public Map<String, Object> serveTest() {
        return Map.of(
            "version", auroraVersion,
            "name", podName
        );
    }

    @GetMapping("/keepalive/client")
    public JsonNode clientTest() {
        StopWatch watch = new StopWatch();
        watch.start();
        try {
            Map<String, String> uriVars = Map.of();
            ResponseEntity<JsonNode> entity = restTemplate.getForEntity("/keepalive/server", JsonNode.class, uriVars);
            watch.stop();
            long totalTimeMillis = watch.getTotalTimeMillis();
            String clientName = "";
            if (entity.getBody() != null && entity.getBody().get("name") != null) {
                clientName = entity.getBody().get("name").asText();
            }
            List<String> strings = entity.getHeaders().get("Keep-Alive");
            logger.info("response={} server={} client={} time={}ms keepalive={}", entity.getStatusCodeValue(), podName, clientName, totalTimeMillis, strings);
            return entity.getBody();
        } catch(Exception e){

            watch.stop();
            long totalTimeMillis = watch.getTotalTimeMillis();
            logger.warn("File skjedde etter tid=" + totalTimeMillis, e);
            return new TextNode("Tom");
        }
    }
}
