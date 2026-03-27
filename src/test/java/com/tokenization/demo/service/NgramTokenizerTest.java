package com.tokenization.demo.service;

import com.tokenization.demo.model.TokenizationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NgramTokenizerTest {

    private NgramTokenizer tokenizer;

    @BeforeEach
    void setUp() {
        tokenizer = new NgramTokenizer();
    }

    @Test
    void getName() {
        assertEquals("ngram", tokenizer.getName());
    }

    @Test
    void generatesTrigrams() {
        TokenizationResult result = tokenizer.tokenize("hello");
        assertEquals(3, result.tokens().size());
        assertEquals("hel", result.tokens().get(0));
        assertEquals("ell", result.tokens().get(1));
        assertEquals("llo", result.tokens().get(2));
    }

    @Test
    void shortInput() {
        TokenizationResult result = tokenizer.tokenize("ab");
        assertTrue(result.tokens().isEmpty());
    }

    @Test
    void exactlyThreeChars() {
        TokenizationResult result = tokenizer.tokenize("abc");
        assertEquals(1, result.tokens().size());
        assertEquals("abc", result.tokens().get(0));
    }

    @Test
    void normalizesCase() {
        TokenizationResult result = tokenizer.tokenize("ABC");
        assertEquals("abc", result.tokens().get(0));
    }
}
