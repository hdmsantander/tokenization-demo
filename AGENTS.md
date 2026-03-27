# AGENTS.md

## Cursor Cloud specific instructions

### Project overview

Java 21 / Spring Boot 3.2.5 web application — a search engine tokenization demo. Single service, no external dependencies (databases, caches, etc.).

### Key commands

| Task | Command |
|------|---------|
| Compile | `mvn compile` |
| Run tests | `mvn test` |
| Lint (Checkstyle) | `mvn checkstyle:check` |
| Run dev server | `mvn spring-boot:run` (port 8080) |
| Full build | `mvn package` |

### Notes

- Maven must be installed (`sudo apt-get install -y maven`). The VM may not have it pre-installed.
- The app runs on port **8080** with Thymeleaf templates and Spring Boot DevTools for live reload.
- REST API available at `/api/tokenizers` (list) and `/api/tokenize` (POST with JSON body `{"query": "...", "tokenizers": [...]}`).
- No database or external service required — fully self-contained.
