Run the full project build using Gradle, skipping tests for speed:

```bash
./gradlew build -DAPI_SPEC_VERSION=local -x test
```

If the build fails, report the error output clearly.