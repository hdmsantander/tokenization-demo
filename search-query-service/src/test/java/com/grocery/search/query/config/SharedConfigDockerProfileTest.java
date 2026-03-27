package com.grocery.search.query.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("docker")
class SharedConfigDockerProfileTest {

  @Autowired private Environment env;

  @Test
  void dockerProfile_usesComposeServiceHostnames() {
    assertThat(env.getProperty("grocery.elasticsearch.uris")).isEqualTo("http://elasticsearch:9200");
    assertThat(env.getProperty("grocery.kafka.bootstrap-servers")).isEqualTo("kafka:9092");
    assertThat(env.getProperty("grocery.postgres.host")).isEqualTo("postgres");
    assertThat(env.getProperty("grocery.redis.host")).isEqualTo("redis");
  }

  @Test
  void springDockerComposeAutoConfigDisabled() {
    assertThat(env.getProperty("spring.docker.compose.enabled", Boolean.class)).isFalse();
  }
}
