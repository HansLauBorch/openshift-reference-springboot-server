package no.skatteetaten.aurora.openshift.reference.springboot.service;

import static org.junit.jupiter.api.Assertions.*;

import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseProvider.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;

@JdbcTest
@AutoConfigureEmbeddedDatabase(provider = ZONKY)
class CounterDatabaseServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Verify maintains counter")
    void verifyCounter() {
        CounterDatabaseService service = new CounterDatabaseService(jdbcTemplate);
        Long counter1 = service.getAndIncrementCounter();
        Long counter2 = service.getAndIncrementCounter();

        assertEquals(1, counter1);
        assertEquals(2, counter2);
    }
}