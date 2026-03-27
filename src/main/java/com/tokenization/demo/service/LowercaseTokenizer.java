package com.tokenization.demo.service;

import com.tokenization.demo.model.TokenizationResult;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class LowercaseTokenizer implements Tokenizer {

    @Override
    public String getName() {
        return "lowercase";
    }

    @Override
    public TokenizationResult tokenize(String query) {
        List<String> tokens = Arrays.stream(query.toLowerCase().split("\\s+"))
                .filter(s -> !s.isEmpty())
                .toList();

        return new TokenizationResult(
                query,
                getName(),
                tokens,
                Map.of("description", "Lowercases text then splits on whitespace")
        );
    }
}
