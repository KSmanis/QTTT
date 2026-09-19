# Developer tools

The opening-book generator is a JVM-only tool kept outside the Android source
sets. Gradle compiles it together with the pure game model without packaging it
in the app.

Provision the pinned Java and SQLite tools once:

```sh
mise install java sqlite
```

Compile the tool without running minimax:

```sh
mise exec -- ./gradlew :app:compileOpeningBookGenerator
```

Generate one SQLite opening-book file when needed:

```sh
mise exec -- ./gradlew :app:generateOpeningBook -Pturn=0
```

`turn` must be between `0` and `4`. Each run atomically updates the packaged
`app/src/main/assets/opening-book-v1.db`, preserving the other turns. Generation
is compute-intensive.

The application copies the versioned database asset into private storage in the
background and queries it read-only. Rename the asset and update
`OpeningBook.DATABASE_NAME` when its contents change.

The SQLite representation uses fixed-width, human-readable move codes. Board
cells are numbered `1` through `9`; a regular move uses its two cell digits and
a collapse uses `0` followed by its cell digit. Histories concatenate these
codes into an integer primary key (`0` is the empty history), while candidate
moves concatenate them as text so a leading collapse marker is preserved.
