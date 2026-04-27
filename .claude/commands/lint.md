Run all code quality checks:

1. PMD static analysis:
```bash
./gradlew pmdMain
```

2. OpenAPI spec linting via Spectral:
```bash
spectral lint "src/main/resources/openapi/*.{yml,yaml}"
```

Report any violations found by either tool.