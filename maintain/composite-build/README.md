# MiniConnect composite project

This is a [Gradle composite project](https://docs.gradle.org/current/userguide/composite_builds.html) for all the MiniConnect projects.

You can execute Gradle commands for any of the projects here,
directly using the latest state of the source code
without rebuilding and publishing anything to the local artifact repository.
For example:

```bash
./gradlew holodb:app:run --quiet --console=plain --args='/path/to/config.yaml'
```

However, version numbers must be matching,
so this is useful primarily in a global SNAPSHOT state.
