package com.tokenization.demo.controller;

import com.tokenization.demo.model.TokenizationResult;
import com.tokenization.demo.model.TokenizeRequest;
import com.tokenization.demo.service.TokenizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TokenizationApiController {

    private final TokenizationService tokenizationService;

    public TokenizationApiController(TokenizationService tokenizationService) {
        this.tokenizationService = tokenizationService;
    }

    @GetMapping("/tokenizers")
    public ResponseEntity<List<String>> listTokenizers() {
        return ResponseEntity.ok(tokenizationService.getAvailableTokenizers());
    }

    @PostMapping("/tokenize")
    public ResponseEntity<List<TokenizationResult>> tokenize(@RequestBody TokenizeRequest request) {
        if (request.query() == null || request.query().isBlank()) {
            return ResponseEntity.badRequest().body(List.of());
        }
        List<TokenizationResult> results = tokenizationService.tokenize(
                request.query(), request.tokenizers());
        return ResponseEntity.ok(results);
    }

    @GetMapping("/tokenize/{tokenizer}")
    public ResponseEntity<?> tokenizeSingle(
            @PathVariable String tokenizer,
            @RequestParam String query) {
        try {
            TokenizationResult result = tokenizationService.tokenize(query, tokenizer);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
