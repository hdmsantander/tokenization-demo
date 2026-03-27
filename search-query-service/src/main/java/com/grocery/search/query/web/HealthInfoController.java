package com.grocery.search.query.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Lightweight readiness probe until Elasticsearch wiring lands (Stage B).
 */
@RestController
public class HealthInfoController {

  @Value("${spring.application.name}")
  private String applicationName;

  @GetMapping("/api/v1/meta/info")
  public Map<String, String> info() {
    return Map.of("service", applicationName, "phase", "skeleton");
  }
}
