package com.tokenization.demo.model;

import java.util.List;
import java.util.Map;

public record TokenizationResult(
    String originalQuery,
    String tokenizerName,
    List<String> tokens,
    Map<String, Object> metadata
) {}
