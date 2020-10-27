package no.skatteetaten.aurora.openshift.reference.springboot.controllers;

import static no.skatteetaten.aurora.AuroraMetrics.StatusValue.CRITICAL;
import static no.skatteetaten.aurora.AuroraMetrics.StatusValue.OK;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/*
 * An example controller that shows how to do a REST call and how to do an operation with a operations metrics
 * There should be a metric called http_client_requests http_server_requests and operations
 */
@RestController()
public class KeepAliveController {

    private static final String SOMETIMES = "sometimes";
    private static final int SECOND = 1000;
    private final String podName;
    private final String auroraVersion;
    private final RestTemplateBuilder restTemplateBuilder;

    public KeepAliveController(
        @Value("${pod.name:localhost}") String podName,
        @Value("${aurora.version:local-dev}") String auroraVersion,
        RestTemplateBuilder restTemplateBuilder) {

        this.restTemplateBuilder = restTemplateBuilder;
        this.podName = podName;
        this.auroraVersion = auroraVersion;
    }

    @GetMapping("/ka/server")
    public Map<String, Object> serveTest() {

        return Map.of(
            "version", auroraVersion,
            "name", podName
        );
    }

    @GetMapping("/ka/client/{service}/duration/{duration}")
    public void  clientTest(
        @PathVariable("service") String service,
        @PathVariable("duration") Duration duration
    ) {
        var restTemplate= new RestTemplateBuilder().rootUri("http://" + service).build();



    }


}

