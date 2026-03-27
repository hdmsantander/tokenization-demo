package com.grocery.search.contracts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonSchemaContractTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static JsonSchema loadSchema(String classpathResource) throws Exception {
    try (InputStream in = JsonSchemaContractTest.class.getResourceAsStream(classpathResource)) {
      JsonNode node = MAPPER.readTree(in);
      return JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012).getSchema(node);
    }
  }

  private static JsonNode loadFixture(String classpathResource) throws Exception {
    try (InputStream in = JsonSchemaContractTest.class.getResourceAsStream(classpathResource)) {
      return MAPPER.readTree(in);
    }
  }

  @Test
  void indexItemEnriched_validUpsertFixture_passesSchema() throws Exception {
    JsonSchema schema = loadSchema("/schemas/index-item-enriched.schema.json");
    JsonNode data = loadFixture("/fixtures/index-item-upsert-valid.json");
    Set<com.networknt.schema.ValidationMessage> errors = schema.validate(data);
    assertTrue(errors.isEmpty(), () -> errors.toString());
  }

  @Test
  void indexItemEnriched_validDeleteFixture_passesSchema() throws Exception {
    JsonSchema schema = loadSchema("/schemas/index-item-enriched.schema.json");
    JsonNode data = loadFixture("/fixtures/index-item-delete-valid.json");
    Set<com.networknt.schema.ValidationMessage> errors = schema.validate(data);
    assertTrue(errors.isEmpty(), () -> errors.toString());
  }

  @Test
  void outboxUnwrapped_samplePassesSchema() throws Exception {
    JsonSchema schema = loadSchema("/schemas/outbox-payload-unwrapped.schema.json");
    String json =
        """
        {
          "event_type": "ItemSearchUpsert",
          "event_version": 1,
          "aggregate_id": "t1:s42:SKU-001",
          "tenant_id": "t1",
          "store_id": "s42",
          "sku": "SKU-001",
          "name": "Organic oats",
          "active": true
        }
        """;
    JsonNode data = MAPPER.readTree(json);
    Set<com.networknt.schema.ValidationMessage> errors = schema.validate(data);
    assertTrue(errors.isEmpty(), () -> errors.toString());
  }
}
