package no.skatteetaten.aurora.openshift.reference.springboot.controllers.errorhandling;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ErrorHandlerTest {

    private static final String ERROR_MESSAGE = "Test";
    private static final String ROOT_CAUSE = "Root cause";
    private final ErrorHandler errorHandler = new ErrorHandler();

    @Test
    @DisplayName("Sets correct status code and error message on causeless Exceptions")
    void handleCauselessException() {
        ResponseEntity<Object> response =
            errorHandler.handleBadRequest(new IllegalArgumentException(ERROR_MESSAGE), null);
        String body = String.valueOf(response.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(body.contains(ERROR_MESSAGE));
    }

    @Test
    @DisplayName("Sets cause on Exceptions with a root cause")
    void handleExceptionsWithRootCause() {
        ResponseEntity<Object> response = errorHandler
            .handleBadRequest(new IllegalArgumentException(ERROR_MESSAGE, new IndexOutOfBoundsException(ROOT_CAUSE)),
                null);
        String body = String.valueOf(response.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(body.contains(ERROR_MESSAGE));
        assertTrue(body.contains(ROOT_CAUSE));
    }
}