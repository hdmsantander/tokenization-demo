package com.tokenization.demo.service;

import com.tokenization.demo.model.TokenizationResult;

public interface Tokenizer {

    String getName();

    TokenizationResult tokenize(String query);
}
