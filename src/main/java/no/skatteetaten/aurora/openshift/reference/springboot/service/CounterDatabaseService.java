package no.skatteetaten.aurora.openshift.reference.springboot.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * An example service that demonstrates basic database operations.
 * For some reason if this is called CounterService it will not load?
 */
@Service
public class CounterDatabaseService {

    private final JdbcTemplate jdbcTemplate;

    public CounterDatabaseService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public Long getAndIncrementCounter() {
        jdbcTemplate.update("UPDATE counter SET value=value+1");
        return jdbcTemplate.queryForObject("SELECT value FROM counter FOR UPDATE", Long.class);
    }

    public Long getCounter() {
        Long counter = jdbcTemplate.queryForObject("SELECT value FROM counter", Long.class);
        if (counter == null) {
            return 0L;
        }
        return counter;
    }
}
