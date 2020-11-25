package no.skatteetaten.aurora.openshift.reference.springboot.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import no.skatteetaten.aurora.openshift.reference.springboot.ShutdownHook;

/*
 * An example controller that shows how to do a REST call and how to do an operation with a operations metrics
 * There should be a metric called http_client_requests http_server_requests and operations
 */
@RestController()
public class KeepAliveController {

    private final String podName;
    private final String auroraVersion;
    private final int postSize;
    private final RestTemplate restTemplate;
    private final Logger logger = LoggerFactory.getLogger(KeepAliveController.class);
    private final int number;
    private final int wait;

    public KeepAliveController(
        @Value("${pod.name:localhost}") String podName,
        @Value("${aurora.version:local-dev}") String auroraVersion,
        @Value("${keepalive.wait:200}") int wait,
        @Value("${keepalive.number:1000}") int number,
        @Value("${keepalive.max.postsizebyte:500000}") int postSize,
        RestTemplate restTemplate) {

        this.wait = wait;
        this.number = number;
        this.postSize = postSize;
        this.restTemplate = restTemplate;
        this.podName = podName;
        this.auroraVersion = auroraVersion;
    }

    @GetMapping("/keepalive/server")
    public Map<String, Object> serveTest(HttpServletResponse response) {
        addHeaderConnectionCloseIfApplicable(response);
        logger.info("Received request");
        return Map.of(
            "version", auroraVersion,
            "name", podName
        );
    }

    @PostMapping(value = "/keepalive/post",produces = MediaType.TEXT_PLAIN_VALUE)
    public void post(HttpServletRequest request, HttpServletResponse response) throws IOException {
        addHeaderConnectionCloseIfApplicable(response);
        int count = 0;
        try (InputStream is = request.getInputStream()) {
            int input = 0;
            while ( input != -1) {
                count++;
                input = is.read();
            }
        }
        response.setTrailerFields(trailingSupplier());
        response.getWriter().println("End - read "+count+" bytes successfully.");
    }

    private Supplier<Map<String, String>> trailingSupplier() {
        return () -> {
            if (ShutdownHook.isHookCalled()) {
                logger.info("Shutdown has been called, add connection close to header");
                return Map.of("Connection","close");
            }
            return new HashMap<>();
        };
    }

    private int reqCount = 0;
    /**
     * When testing, I find that this is never call, which is to be expected - i.e. no new calls
     * on an application which is shut down.
     */
    private void addHeaderConnectionCloseIfApplicable(HttpServletResponse response) {
        if (ShutdownHook.isHookCalled()) {
            logger.info("Shutdown has been called, add connection close to header");
            response.addHeader("Connection","close");
        } else if ( reqCount++ > 25 ) {
            logger.info("Send connection close as I have answered 25 requests to see if that alliviates problem");
            response.addHeader("Connection","close");
        }
    }

    @GetMapping("/keepalive/clientpost")
    public void clientPost() {
        for (int i = 0; i < number; i++) {
            String requestId = UUID.randomUUID().toString();
            MDC.put("requestId", requestId);

            StopWatch watch = new StopWatch();
            watch.start();
            try {
                Thread.sleep(wait);
                String randomText = "x".repeat(Math.max(2000, new Random().nextInt(postSize)));
                ResponseEntity<String> entity =
                    restTemplate.postForEntity("/keepalive/post", randomText, String.class);
                watch.stop();
                long totalTimeMillis = watch.getTotalTimeMillis();
                List<String> keepAlive = entity.getHeaders().get("Keep-Alive");
                List<String> connection = entity.getHeaders().get("Connection");
                logger.info("response={} server={} time={}ms keepalive={} connection={}", entity.getStatusCodeValue(),
                    podName,
                    totalTimeMillis, keepAlive, connection);
            } catch (Exception e) {
                watch.stop();
                long totalTimeMillis = watch.getTotalTimeMillis();
                logger.warn("Feil skjedde etter tid=" + totalTimeMillis, e);
            }
        }
        logger.info("Done {} posts", number);
    }

    @GetMapping("/keepalive/client")
    public void clientTest() {
        for (int i = 0; i < number; i++) {
            String requestId = UUID.randomUUID().toString();
            MDC.put("requestId", requestId);

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
                List<String> keepAlive = entity.getHeaders().get("Keep-Alive");
                List<String> connection = entity.getHeaders().get("Connection");
                logger.info("response={} server={} client={} time={}ms keepalive={} connection={}", entity.getStatusCodeValue(),
                    podName, clientName,
                    totalTimeMillis, keepAlive, connection);
            } catch (Exception e) {
                watch.stop();
                long totalTimeMillis = watch.getTotalTimeMillis();
                logger.warn("Feil skjedde etter tid=" + totalTimeMillis, e);
            }
        }
        logger.info("Done {} requests", number);
    }
}
