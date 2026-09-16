# Developer tools

The opening-book generator is a JVM-only tool kept outside the Android source
sets. Gradle compiles it together with the pure game model without packaging it
in the app.

Compile the tool without running minimax:

```sh
./gradlew :app:compileOpeningBookGenerator
```

Generate one opening-book file when needed:

```sh
./gradlew :app:generateOpeningBook -Pturn=0
```

`turn` must be between `0` and `4`. Output is written as `TURN.txt` in the
repository root; turn 4 remains a single file and is not split like the packaged
production assets.
