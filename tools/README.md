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

Convert a legacy opening-book directory to the packaged SQLite database, compare
the two representations, or export readable text with the commands below. The
input may contain the packaged legacy layout or the generator's `0.txt` through
`4.txt` output.

```sh
mise exec -- python tools/opening_book.py build LEGACY_DIRECTORY opening-book-v1.db
mise exec -- python tools/opening_book.py verify LEGACY_DIRECTORY opening-book-v1.db
mise exec -- python tools/opening_book.py export opening-book-v1.db EMPTY_DIRECTORY
```

The application copies the versioned database asset into private storage in the
background and queries it read-only. Rename the asset and update
`OpeningBook.DATABASE_NAME` when its contents change.
