package com.grocery.search.config;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** CI smoke: central YAML is packaged and contains expected integration keys. */
class GroceryDefaultsResourceTest {

  @Test
  void groceryDefaultsYmlIsOnClasspath() throws Exception {
    try (InputStream in = getClass().getResourceAsStream("/config/grocery-defaults.yml")) {
      assertNotNull(in, "config/grocery-defaults.yml must be on classpath");
      String yaml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
      assertTrue(yaml.contains("grocery:"));
      assertTrue(yaml.contains("inventory.outbox.events"));
      assertTrue(yaml.contains("docker:") && yaml.contains("compose:") && yaml.contains("enabled: false"));
    }
  }
}
