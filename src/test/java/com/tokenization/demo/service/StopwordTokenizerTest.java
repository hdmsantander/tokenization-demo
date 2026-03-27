package com.tokenization.demo.service;

import com.tokenization.demo.model.TokenizationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StopwordTokenizerTest {

    private StopwordTokenizer tokenizer;

    @BeforeEach
    void setUp() {
        tokenizer = new StopwordTokenizer();
    }

    @Test
    void getName() {
        assertEquals("stopword", tokenizer.getName());
    }

    @Test
    void removesStopWords() {
        TokenizationResult result = tokenizer.tokenize("the quick brown fox");
        assertFalse(result.tokens().contains("the"));
        assertTrue(result.tokens().contains("quick"));
        assertTrue(result.tokens().contains("brown"));
        assertTrue(result.tokens().contains("fox"));
    }

    @Test
    void allStopWords() {
        TokenizationResult result = tokenizer.tokenize("the is a an");
        assertTrue(result.tokens().isEmpty());
    }

    @Test
    void noStopWords() {
        TokenizationResult result = tokenizer.tokenize("java spring boot");
        assertEquals(3, result.tokens().size());
    }
}
