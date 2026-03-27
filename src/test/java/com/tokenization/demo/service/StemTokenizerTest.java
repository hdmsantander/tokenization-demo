package com.tokenization.demo.service;

import com.tokenization.demo.model.TokenizationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StemTokenizerTest {

    private StemTokenizer tokenizer;

    @BeforeEach
    void setUp() {
        tokenizer = new StemTokenizer();
    }

    @Test
    void getName() {
        assertEquals("stem", tokenizer.getName());
    }

    @Test
    void stemsIngSuffix() {
        assertEquals("runn", tokenizer.simpleStem("running"));
    }

    @Test
    void stemsEdSuffix() {
        assertEquals("jump", tokenizer.simpleStem("jumped"));
    }

    @Test
    void stemsSSuffix() {
        assertEquals("cat", tokenizer.simpleStem("cats"));
    }

    @Test
    void stemsIesSuffix() {
        assertEquals("berry", tokenizer.simpleStem("berries"));
    }

    @Test
    void preservesShortWords() {
        assertEquals("the", tokenizer.simpleStem("the"));
        assertEquals("is", tokenizer.simpleStem("is"));
    }

    @Test
    void tokenizeFullQuery() {
        TokenizationResult result = tokenizer.tokenize("running cats jumped");
        assertEquals(List.of("runn", "cat", "jump"), result.tokens());
    }
}
