package no.skatteetaten.aurora.openshift.reference.springboot.health;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.actuate.health.Health;

import no.skatteetaten.aurora.openshift.reference.springboot.service.CounterDatabaseService;

class CounterHealthTest {
    private CounterDatabaseService databaseService = mock(CounterDatabaseService.class);
    private CounterHealth counterHealth = new CounterHealth(databaseService);

    @ParameterizedTest
    @EnumSource
    @DisplayName("Verifies status varies on counter value")
    void counterStatus(CounterStatus counterStatus) {
        given(databaseService.getCounter()).willReturn(counterStatus.counter);

        Health health = counterHealth.health();

        assertEquals(counterStatus.name(), health.getStatus().getCode());
    }

    enum CounterStatus {
        OBSERVE(2),
        UP(3);

        long counter;

        CounterStatus(long counter) {
            this.counter = counter;
        }
    }
}