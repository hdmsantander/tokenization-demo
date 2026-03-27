package com.grocery.search.query;

import org.junit.jupiter.api.Test;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates Docker + Testcontainers can run Elasticsearch (same family as compose infra).
 */
@Testcontainers(disabledWithoutDocker = true)
class ElasticsearchInfraIntegrationTest {

  private static final DockerImageName ES_IMAGE =
      DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.12.2")
          .asCompatibleSubstituteFor("docker.elastic.co/elasticsearch/elasticsearch");

  @Container
  static final ElasticsearchContainer ES =
      new ElasticsearchContainer(ES_IMAGE)
          .withEnv("xpack.security.enabled", "false")
          .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m");

  @Test
  void clusterReportsHealthy() throws Exception {
    String base = ES.getHttpHostAddress();
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest req = HttpRequest.newBuilder(URI.create("http://" + base + "/_cluster/health")).GET().build();
    HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
    assertTrue(res.statusCode() == 200, "status: " + res.statusCode());
    assertTrue(res.body().contains("cluster_name"), res.body());
  }
}
