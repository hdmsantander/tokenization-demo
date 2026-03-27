package com.tokenization.demo.model;

import java.util.List;

public record TokenizeRequest(
    String query,
    List<String> tokenizers
) {}
