package com.tokenization.demo.controller;

import com.tokenization.demo.model.TokenizationResult;
import com.tokenization.demo.service.TokenizationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class TokenizationController {

    private final TokenizationService tokenizationService;

    public TokenizationController(TokenizationService tokenizationService) {
        this.tokenizationService = tokenizationService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("tokenizers", tokenizationService.getAvailableTokenizers());
        return "index";
    }

    @GetMapping("/tokenize")
    public String tokenize(
            @RequestParam String query,
            @RequestParam(required = false) List<String> tokenizers,
            Model model) {
        List<TokenizationResult> results = tokenizationService.tokenize(query, tokenizers);
        model.addAttribute("query", query);
        model.addAttribute("results", results);
        model.addAttribute("tokenizers", tokenizationService.getAvailableTokenizers());
        model.addAttribute("selectedTokenizers", tokenizers);
        return "index";
    }
}
