package no.skatteetaten.aurora.openshift.reference.springboot.psa;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;


public class OpenShiftTokenReviewService {
    private static final Logger logger = LoggerFactory.getLogger(OpenShiftTokenReviewService.class);

    private static final ObjectWriter REVIEW_WRITER = new ObjectMapper().writerFor(TokenReview.class);
    private static final ObjectReader REVIEW_READER = new ObjectMapper().readerFor(TokenReview.class);
    private final HttpClient httpClient;
    private final String serviceTokenPath;
    private final String tokenReviewUrl;

    public OpenShiftTokenReviewService(HttpClient client, String serviceTokenPath, String tokenReviewUrl) {
        this.httpClient = client;
        this.serviceTokenPath = serviceTokenPath;
        this.tokenReviewUrl = tokenReviewUrl;
    }

    public boolean matchesScheme(String authHeader) {
        return authHeader != null && authHeader.startsWith("Bearer ");
    }

    public boolean isValid(String authHeader) {
        try {
            return isValidImpl(authHeader);
        } catch (IOException | IllegalArgumentException | InterruptedException e) {
            logger.error("Error processing token", e);
            return false;
        }
    }

    private boolean isValidImpl(String authHeader) throws IOException, InterruptedException {
        TokenReview tokenReview = new TokenReview(getToken(authHeader));

        HttpRequest post = HttpRequest.newBuilder()
            .uri(URI.create(tokenReviewUrl))
            .timeout(Duration.of(15, ChronoUnit.SECONDS))
            .header("Authorization", "Bearer " + Files.readString(Paths.get(serviceTokenPath)))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            //Used to set Manager field in the returned structure
            .header("User-Agent", "k15321-agent") // TODO Use something sensible, gets logged as manager
            .POST(HttpRequest.BodyPublishers.ofString(REVIEW_WRITER.writeValueAsString(tokenReview)))
            .build();

        HttpResponse<String> response = httpClient.send(post, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 == 2) {
            String reviewAsString = response.body();
            logger.info("-- tokenreview --------> "+reviewAsString); // TODO Temporary
            TokenReview review = REVIEW_READER.readValue(reviewAsString);
            if (!review.status.authenticated) {
                logger.warn("Token is not authenticated");
                return false;
            }
            return hasAccess(review.status.user.username);
        } else {
            logger.error("Unable to call TokenReview API: HTTP code {}, response: {}", response.statusCode(),
                response.body());
            return false;
        }
    }


    /**
     * TODO Shall this be included at all? In what form, in that case, abstract method, perhaps??
     * The username is typically the full service account name, such as system:serviceaccount:demo-keepalive:default
     * <b>Currently accepting any service account</b>
     */
    private boolean hasAccess(String username) {
        //if (username.startsWith("system:serviceaccount:aurora:")) {
        // Currently accepting any service account
        logger.info("SA: "+username);
        if (username.startsWith("system:serviceaccount:")) {
            return true;
        } else {
            logger.warn("User {} has no access", username);
            return false;
        }
    }

    private String getToken(String authHeader) {
        if ( authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Unable to parse token");
        }
        return authHeader.substring("Bearer ".length());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TokenReview {
        public String kind = "TokenReview";
        public String apiVersion = "authentication.k8s.io/v1";
        public Spec spec;
        public Status status;
        public TokenReview(String token) {
            spec = new Spec(token);
        }

        public TokenReview() {
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Spec {
        public String token;

        public Spec(String token) {
            this.token = token;
        }

        public Spec() {
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Status {
        public boolean authenticated;
        public User user;
        public List<String> audiences;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class User {
        public String username;
    }

}
