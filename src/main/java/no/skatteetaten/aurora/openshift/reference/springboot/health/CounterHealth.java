package no.skatteetaten.aurora.openshift.reference.springboot.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import no.skatteetaten.aurora.openshift.reference.springboot.service.CounterDatabaseService;

/**
 * A sample custom health check. You can add your own health checks that verifies the proper operational status of your
 * application.
 */
@Component
public class CounterHealth implements HealthIndicator {

    private final CounterDatabaseService counterDatabaseService;

    public CounterHealth(CounterDatabaseService counterDatabaseService) {
        this.counterDatabaseService = counterDatabaseService;
    }

    @Override
    public Health health() {
        Long currentValue = counterDatabaseService.getCounter();

        if (currentValue % 2 == 0) {
            return Health.status("OBSERVE").withDetail("message", "Partall antall i teller")
                .withDetail("Antall", currentValue).build();
        } else {
            return Health.up().withDetail("Antall", currentValue).build();
        }
    }
}
