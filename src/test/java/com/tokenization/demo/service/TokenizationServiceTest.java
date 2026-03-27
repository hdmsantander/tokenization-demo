package com.tokenization.demo.service;

import com.tokenization.demo.model.TokenizationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TokenizationServiceTest {

    private TokenizationService service;

    @BeforeEach
    void setUp() {
        service = new TokenizationService(List.of(
                new WhitespaceTokenizer(),
                new LowercaseTokenizer(),
                new NgramTokenizer(),
                new StopwordTokenizer(),
                new StemTokenizer()
        ));
    }

    @Test
    void listAllTokenizers() {
        List<String> names = service.getAvailableTokenizers();
        assertEquals(5, names.size());
        assertTrue(names.contains("whitespace"));
        assertTrue(names.contains("lowercase"));
        assertTrue(names.contains("ngram"));
        assertTrue(names.contains("stopword"));
        assertTrue(names.contains("stem"));
    }

    @Test
    void tokenizeWithAllTokenizers() {
        List<TokenizationResult> results = service.tokenize("hello world", (List<String>) null);
        assertEquals(5, results.size());
    }

    @Test
    void tokenizeWithSpecificTokenizers() {
        List<TokenizationResult> results = service.tokenize(
                "hello world", List.of("whitespace", "lowercase"));
        assertEquals(2, results.size());
    }

    @Test
    void tokenizeSingle() {
        TokenizationResult result = service.tokenize("hello world", "whitespace");
        assertEquals("whitespace", result.tokenizerName());
        assertEquals(List.of("hello", "world"), result.tokens());
    }

    @Test
    void tokenizeSingleUnknown() {
        assertThrows(IllegalArgumentException.class,
                () -> service.tokenize("hello", "nonexistent"));
    }
}
