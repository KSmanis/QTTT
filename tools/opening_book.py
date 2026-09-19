#!/usr/bin/env python3
import argparse
import os
import sqlite3
from pathlib import Path


SCHEMA = """
CREATE TABLE opening_book (
    history TEXT PRIMARY KEY,
    utility_value INTEGER NOT NULL,
    utility_depth INTEGER NOT NULL,
    moves TEXT NOT NULL
) WITHOUT ROWID;
"""


def validate_move(move):
    cells = move.split(",")
    if len(cells) not in (1, 2) or any(not 0 <= int(cell) < 9 for cell in cells):
        raise ValueError(f"Invalid move: {move}")


def record(history, utility, depth, moves):
    if history != "()":
        if not history.startswith("(") or not history.endswith(")"):
            raise ValueError(f"Invalid history: {history}")
        for move in history[1:-1].split(")("):
            validate_move(move)
    if not moves:
        raise ValueError(f"Position has no moves: {history}")
    for move in moves:
        validate_move(move)
    return history, utility, depth, "\n".join(moves)


def legacy_records(directory):
    directory = Path(directory)
    paths = [
        directory / str(turn)
        if (directory / str(turn)).is_file()
        else directory / f"{turn}.txt"
        for turn in range(4)
    ]
    if (directory / "4").is_dir():
        paths.extend(sorted(path for path in (directory / "4").iterdir() if path.is_file()))
    else:
        paths.append(directory / "4.txt")
    for path in paths:
        history = None
        moves = []
        with path.open(encoding="utf-8") as source:
            for raw_line in source:
                line = raw_line.rstrip("\n")
                if line.startswith("("):
                    if history is not None:
                        yield record(history, utility, depth, moves)
                    history, score = line.rsplit(":", 1)
                    utility, depth = map(int, score.split(","))
                    moves = []
                elif line:
                    moves.append(line)
        if history is not None:
            yield record(history, utility, depth, moves)


def build(source, destination):
    destination = Path(destination)
    temporary = destination.with_suffix(destination.suffix + ".tmp")
    temporary.unlink(missing_ok=True)
    database = sqlite3.connect(temporary)
    database.executescript(
        "PRAGMA journal_mode=OFF; PRAGMA synchronous=OFF; PRAGMA temp_store=MEMORY;"
        + SCHEMA
    )
    with database:
        database.executemany("INSERT INTO opening_book VALUES (?, ?, ?, ?)", legacy_records(source))
    database.execute("PRAGMA user_version=1")
    database.execute("VACUUM")
    database.close()
    os.replace(temporary, destination)


def verify(source, database_path):
    database = sqlite3.connect(f"file:{Path(database_path).resolve()}?mode=ro", uri=True)
    query = "SELECT utility_value, utility_depth, moves FROM opening_book WHERE history = ?"
    count = 0
    for history, utility, depth, moves in legacy_records(source):
        if database.execute(query, (history,)).fetchone() != (utility, depth, moves):
            raise ValueError(f"Opening book differs at {history}")
        count += 1
    database_count = database.execute("SELECT count(*) FROM opening_book").fetchone()[0]
    integrity = database.execute("PRAGMA integrity_check").fetchone()[0]
    database.close()
    if count != database_count:
        raise ValueError(f"Expected {count} positions, found {database_count}")
    if integrity != "ok":
        raise ValueError(integrity)
    print(f"Verified {count} positions")


def export(database_path, destination):
    destination = Path(destination)
    destination.mkdir(parents=True, exist_ok=True)
    if any(destination.iterdir()):
        raise ValueError(f"Destination is not empty: {destination}")

    database = sqlite3.connect(f"file:{Path(database_path).resolve()}?mode=ro", uri=True)
    files = {}
    try:
        for history, utility, depth, moves in database.execute(
            "SELECT history, utility_value, utility_depth, moves FROM opening_book"
        ):
            turn = history.count(",")
            relative_path = (
                Path(str(turn))
                if turn < 4
                else Path("4") / history[: history.index(")") + 1]
            )
            output = files.get(relative_path)
            if output is None:
                path = destination / relative_path
                path.parent.mkdir(parents=True, exist_ok=True)
                output = files[relative_path] = path.open("w", encoding="utf-8", newline="\n")
            output.write(f"{history}:{utility},{depth}\n{moves}\n\n")
    finally:
        database.close()
        for output in files.values():
            output.close()


def main():
    parser = argparse.ArgumentParser(description="Build and inspect the QTTT opening book")
    commands = parser.add_subparsers(dest="command", required=True)
    for command in ("build", "verify"):
        subparser = commands.add_parser(command)
        subparser.add_argument("legacy_directory")
        subparser.add_argument("database")
    subparser = commands.add_parser("export")
    subparser.add_argument("database")
    subparser.add_argument("legacy_directory")
    args = parser.parse_args()
    if args.command == "build":
        build(args.legacy_directory, args.database)
    elif args.command == "verify":
        verify(args.legacy_directory, args.database)
    else:
        export(args.database, args.legacy_directory)


if __name__ == "__main__":
    main()
