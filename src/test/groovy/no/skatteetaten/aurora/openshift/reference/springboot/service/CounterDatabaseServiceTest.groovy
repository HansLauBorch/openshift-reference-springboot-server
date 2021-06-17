package no.skatteetaten.aurora.openshift.reference.springboot.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.jdbc.core.JdbcTemplate

import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import spock.lang.Specification

import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseProvider.ZONKY

@JdbcTest
@AutoConfigureEmbeddedDatabase(provider = ZONKY)
class CounterDatabaseServiceTest extends Specification {

  @Autowired
  JdbcTemplate jdbcTemplate

  def "Verify maintains counter"() {

    given:
      def service = new CounterDatabaseService(jdbcTemplate)

    when:
      def counter = service.getAndIncrementCounter()

    then:
      counter == 1
      service.counter == 1

    when:
      counter = service.getAndIncrementCounter()

    then:
      counter == 2
      service.counter == 2
  }
}
