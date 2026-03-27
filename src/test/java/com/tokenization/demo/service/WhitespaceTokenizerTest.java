package com.tokenization.demo.service;

import com.tokenization.demo.model.TokenizationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WhitespaceTokenizerTest {

    private WhitespaceTokenizer tokenizer;

    @BeforeEach
    void setUp() {
        tokenizer = new WhitespaceTokenizer();
    }

    @Test
    void getName() {
        assertEquals("whitespace", tokenizer.getName());
    }

    @Test
    void tokenizeSimple() {
        TokenizationResult result = tokenizer.tokenize("hello world");
        assertEquals(List.of("hello", "world"), result.tokens());
    }

    @Test
    void tokenizeMultipleSpaces() {
        TokenizationResult result = tokenizer.tokenize("hello   world   test");
        assertEquals(List.of("hello", "world", "test"), result.tokens());
    }

    @Test
    void tokenizePreservesCase() {
        TokenizationResult result = tokenizer.tokenize("Hello World");
        assertEquals(List.of("Hello", "World"), result.tokens());
    }

    @Test
    void tokenizeEmptyString() {
        TokenizationResult result = tokenizer.tokenize("");
        assertTrue(result.tokens().isEmpty());
    }

    @Test
    void tokenizeSingleWord() {
        TokenizationResult result = tokenizer.tokenize("hello");
        assertEquals(List.of("hello"), result.tokens());
    }
}
