package com.tokenization.demo.service;

import com.tokenization.demo.model.TokenizationResult;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class StopwordTokenizer implements Tokenizer {

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "is", "are", "was", "were", "be", "been", "being",
            "have", "has", "had", "do", "does", "did", "will", "would", "could",
            "should", "may", "might", "shall", "can", "need", "dare", "ought",
            "used", "to", "of", "in", "for", "on", "with", "at", "by", "from",
            "as", "into", "through", "during", "before", "after", "above", "below",
            "between", "out", "off", "over", "under", "again", "further", "then",
            "once", "here", "there", "when", "where", "why", "how", "all", "both",
            "each", "few", "more", "most", "other", "some", "such", "no", "nor",
            "not", "only", "own", "same", "so", "than", "too", "very", "just",
            "because", "but", "and", "or", "if", "while", "about", "up", "it",
            "its", "this", "that", "these", "those", "i", "me", "my", "we", "our",
            "you", "your", "he", "him", "his", "she", "her", "they", "them", "their",
            "what", "which", "who", "whom"
    );

    @Override
    public String getName() {
        return "stopword";
    }

    @Override
    public TokenizationResult tokenize(String query) {
        List<String> tokens = Arrays.stream(query.toLowerCase().split("\\s+"))
                .filter(s -> !s.isEmpty())
                .filter(s -> !STOP_WORDS.contains(s))
                .toList();

        return new TokenizationResult(
                query,
                getName(),
                tokens,
                Map.of(
                        "description", "Lowercases, splits on whitespace, removes common English stop words",
                        "stopWordCount", STOP_WORDS.size()
                )
        );
    }
}
