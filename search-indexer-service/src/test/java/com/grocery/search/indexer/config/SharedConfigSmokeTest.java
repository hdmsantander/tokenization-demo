package com.grocery.search.indexer.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

/** Fast CI check: shared-config import and grocery.* resolution (no Docker). */
@SpringBootTest
class SharedConfigSmokeTest {

  @Autowired private Environment env;

  @Test
  void groceryKafkaTopicsAndElasticsearchResolve() {
    assertThat(env.getProperty("grocery.kafka-topics.item-enriched")).isEqualTo("search.item.enriched");
    assertThat(env.getProperty("grocery.elasticsearch.uris")).isEqualTo("http://localhost:9200");
    assertThat(env.getProperty("spring.docker.compose.enabled", Boolean.class)).isFalse();
  }
}
