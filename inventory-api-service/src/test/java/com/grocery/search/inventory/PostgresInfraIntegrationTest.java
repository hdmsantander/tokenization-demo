package com.grocery.search.inventory;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Validates Docker + Testcontainers can run PostgreSQL (same image family as compose infra).
 */
@Testcontainers(disabledWithoutDocker = true)
class PostgresInfraIntegrationTest {

  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("inventory")
          .withUsername("inventory")
          .withPassword("inventory");

  @Test
  void canQueryVersion() throws Exception {
    try (Connection c = POSTGRES.createConnection("");
        Statement st = c.createStatement();
        ResultSet rs = st.executeQuery("SELECT 1")) {
      assertEquals(true, rs.next());
      assertEquals(1, rs.getInt(1));
    }
  }
}
