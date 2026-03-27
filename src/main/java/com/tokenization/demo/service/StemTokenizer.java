package com.tokenization.demo.service;

import com.tokenization.demo.model.TokenizationResult;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class StemTokenizer implements Tokenizer {

    @Override
    public String getName() {
        return "stem";
    }

    @Override
    public TokenizationResult tokenize(String query) {
        List<String> tokens = Arrays.stream(query.toLowerCase().split("\\s+"))
                .filter(s -> !s.isEmpty())
                .map(this::simpleStem)
                .toList();

        return new TokenizationResult(
                query,
                getName(),
                tokens,
                Map.of("description",
                        "Lowercases, splits on whitespace, applies simple suffix-stripping stemming")
        );
    }

    String simpleStem(String word) {
        if (word.length() <= 3) {
            return word;
        }
        if (word.endsWith("ies") && word.length() > 4) {
            return word.substring(0, word.length() - 3) + "y";
        }
        if (word.endsWith("ing") && word.length() > 5) {
            return word.substring(0, word.length() - 3);
        }
        if (word.endsWith("tion") && word.length() > 5) {
            return word.substring(0, word.length() - 4);
        }
        if (word.endsWith("ness") && word.length() > 5) {
            return word.substring(0, word.length() - 4);
        }
        if (word.endsWith("ed") && word.length() > 4) {
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("ly") && word.length() > 4) {
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("er") && word.length() > 4) {
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("es") && word.length() > 4) {
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("s") && !word.endsWith("ss") && word.length() > 3) {
            return word.substring(0, word.length() - 1);
        }
        return word;
    }
}
