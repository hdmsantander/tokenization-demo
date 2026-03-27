package com.grocery.search.query.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ensures shared-config is on the classpath and {@code grocery.*} resolves for default and docker profiles.
 */
@SpringBootTest
class SharedConfigIntegrationTest {

  @Autowired private Environment env;

  @Test
  void defaultProfile_usesLocalhostElasticsearchUri() {
    assertThat(env.getProperty("grocery.elasticsearch.uris")).isEqualTo("http://localhost:9200");
    assertThat(env.getProperty("grocery.kafka.bootstrap-servers")).isEqualTo("localhost:9092");
    assertThat(env.getProperty("grocery.kafka-topics.outbox-events")).isEqualTo("inventory.outbox.events");
  }
}
