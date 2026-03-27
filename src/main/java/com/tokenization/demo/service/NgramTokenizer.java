package com.tokenization.demo.service;

import com.tokenization.demo.model.TokenizationResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class NgramTokenizer implements Tokenizer {

    private static final int DEFAULT_N = 3;

    @Override
    public String getName() {
        return "ngram";
    }

    @Override
    public TokenizationResult tokenize(String query) {
        String normalized = query.toLowerCase().replaceAll("\\s+", " ").trim();
        List<String> tokens = new ArrayList<>();

        for (int i = 0; i <= normalized.length() - DEFAULT_N; i++) {
            tokens.add(normalized.substring(i, i + DEFAULT_N));
        }

        return new TokenizationResult(
                query,
                getName(),
                tokens,
                Map.of(
                        "description", "Generates character-level n-grams",
                        "n", DEFAULT_N
                )
        );
    }
}
