# tokenization-demo

A search engine tokenization demo for natural language queries.

## Overview

Interactive web application that demonstrates how search engines break down natural language text into tokens using different tokenization strategies.

### Available Tokenizers

| Tokenizer | Description |
|-----------|-------------|
| **whitespace** | Splits text on whitespace characters |
| **lowercase** | Lowercases text then splits on whitespace |
| **ngram** | Generates character-level trigrams |
| **stopword** | Removes common English stop words after lowercasing and splitting |
| **stem** | Applies simple suffix-stripping stemming |

## Prerequisites

- Java 21+
- Maven 3.8+

## Quick Start

```bash
mvn spring-boot:run
```

Open http://localhost:8080 in your browser.

## Development

```bash
# Compile
mvn compile

# Run tests
mvn test

# Lint
mvn checkstyle:check

# Package
mvn package
```

## REST API

```bash
# List available tokenizers
curl http://localhost:8080/api/tokenizers

# Tokenize with all tokenizers
curl -X POST http://localhost:8080/api/tokenize \
  -H "Content-Type: application/json" \
  -d '{"query": "your search query here"}'

# Tokenize with specific tokenizers
curl -X POST http://localhost:8080/api/tokenize \
  -H "Content-Type: application/json" \
  -d '{"query": "your search query", "tokenizers": ["whitespace", "stem"]}'

# Single tokenizer
curl "http://localhost:8080/api/tokenize/stem?query=running+cats"
```

## License

MIT
