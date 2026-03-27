package com.tokenization.demo.service;

import com.tokenization.demo.model.TokenizationResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TokenizationService {

    private final Map<String, Tokenizer> tokenizers;

    public TokenizationService(List<Tokenizer> tokenizerList) {
        this.tokenizers = tokenizerList.stream()
                .collect(Collectors.toMap(Tokenizer::getName, Function.identity()));
    }

    public List<String> getAvailableTokenizers() {
        return tokenizers.keySet().stream().sorted().toList();
    }

    public List<TokenizationResult> tokenize(String query, List<String> tokenizerNames) {
        List<String> names = (tokenizerNames == null || tokenizerNames.isEmpty())
                ? getAvailableTokenizers()
                : tokenizerNames;

        return names.stream()
                .filter(tokenizers::containsKey)
                .map(name -> tokenizers.get(name).tokenize(query))
                .toList();
    }

    public TokenizationResult tokenize(String query, String tokenizerName) {
        Tokenizer tokenizer = tokenizers.get(tokenizerName);
        if (tokenizer == null) {
            throw new IllegalArgumentException("Unknown tokenizer: " + tokenizerName);
        }
        return tokenizer.tokenize(query);
    }
}
