package com.gmail.smanis.konstantinos.qttt;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;

public final class OpeningBookGenerator extends State {
    private static final Path DATABASE = Path.of("app/src/main/assets/opening-book-v1.db");
    private final PrintStream output;

    private OpeningBookGenerator(PrintStream output) {
        this.output = output;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: generate-opening-book TURN (0-4)");
        }

        int turn = parseTurn(args[0]);
        Path destination = DATABASE;
        Path temporary = destination.resolveSibling(destination.getFileName() + ".tmp");
        if (Files.exists(destination)) {
            Files.copy(destination, temporary, StandardCopyOption.REPLACE_EXISTING);
        } else {
            Files.deleteIfExists(temporary);
        }

        Process sqlite =
                new ProcessBuilder("sqlite3", temporary.toString())
                        .redirectError(ProcessBuilder.Redirect.INHERIT)
                        .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                        .start();
        boolean wroteSql = false;
        boolean complete = false;
        try (PrintStream output =
                new PrintStream(
                        new BufferedOutputStream(sqlite.getOutputStream()),
                        false,
                        StandardCharsets.UTF_8)) {
            output.println(".bail on");
            output.println("PRAGMA journal_mode=OFF;");
            output.println("PRAGMA synchronous=OFF;");
            output.println("PRAGMA temp_store=MEMORY;");
            output.println(
                    "CREATE TABLE IF NOT EXISTS opening_book ("
                            + "history INTEGER PRIMARY KEY,"
                            + "utility_value INTEGER NOT NULL,"
                            + "utility_depth INTEGER NOT NULL,"
                            + "moves TEXT NOT NULL CHECK (length(moves) % 2 = 0));");
            output.println("BEGIN;");

            OpeningBookGenerator generator = new OpeningBookGenerator(output);
            generator.generate(turn);

            output.println("COMMIT;");
            output.println("PRAGMA user_version=1;");
            output.println("VACUUM;");
            if (output.checkError()) {
                throw new IOException("Failed to write SQLite input");
            }
            wroteSql = true;
        } finally {
            try {
                int exitCode = sqlite.waitFor();
                if (wroteSql && exitCode == 0) {
                    Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
                    complete = true;
                }
            } finally {
                if (!complete) {
                    sqlite.destroyForcibly();
                    Files.deleteIfExists(temporary);
                }
            }
        }

        if (!complete) {
            throw new IOException("sqlite3 failed to create " + destination);
        }
    }

    private static int parseTurn(String value) {
        int turn;
        try {
            turn = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("TURN must be between 0 and 4", e);
        }
        if (turn < 0 || turn > 4) {
            throw new IllegalArgumentException("TURN must be between 0 and 4");
        }
        return turn;
    }

    private void generate(int turn) {
        switch (turn) {
            case 0:
                printMoves();
                break;
            case 1:
                for (Move m1 : availableMoves(false)) {
                    applyMove(m1);
                    printMoves();
                    undoLastMove();
                }
                break;
            case 2:
                for (Move m1 : availableMoves(false)) {
                    applyMove(m1);
                    for (Move m2 : availableMoves(false)) {
                        applyMove(m2);
                        printMoves();
                        if (entangled()) {
                            for (Move m2e : availableMoves(false)) {
                                applyMove(m2e);
                                printMoves();
                                undoLastMove();
                            }
                        }
                        undoLastMove();
                    }
                    undoLastMove();
                }
                break;
            case 3:
                for (Move m1 : availableMoves(false)) {
                    applyMove(m1);
                    for (Move m2 : availableMoves(false)) {
                        applyMove(m2);
                        if (entangled()) {
                            for (Move m2e : availableMoves(false)) {
                                applyMove(m2e);
                                for (Move m3 : availableMoves(false)) {
                                    applyMove(m3);
                                    printMoves();
                                    undoLastMove();
                                }
                                undoLastMove();
                            }
                        } else {
                            for (Move m3 : availableMoves(false)) {
                                applyMove(m3);
                                printMoves();
                                if (entangled()) {
                                    for (Move m3e : availableMoves(false)) {
                                        applyMove(m3e);
                                        printMoves();
                                        undoLastMove();
                                    }
                                }
                                undoLastMove();
                            }
                        }
                        undoLastMove();
                    }
                    undoLastMove();
                }
                break;
            case 4:
                for (Move m1 : availableMoves(false)) {
                    applyMove(m1);
                    for (Move m2 : availableMoves(false)) {
                        applyMove(m2);
                        if (entangled()) {
                            for (Move m2e : availableMoves(false)) {
                                applyMove(m2e);
                                for (Move m3 : availableMoves(false)) {
                                    applyMove(m3);
                                    for (Move m4 : availableMoves(false)) {
                                        applyMove(m4);
                                        printMoves();
                                        if (entangled()) {
                                            for (Move m4e : availableMoves(false)) {
                                                applyMove(m4e);
                                                printMoves();
                                                undoLastMove();
                                            }
                                        }
                                        undoLastMove();
                                    }
                                    undoLastMove();
                                }
                                undoLastMove();
                            }
                        } else {
                            for (Move m3 : availableMoves(false)) {
                                applyMove(m3);
                                if (entangled()) {
                                    for (Move m3e : availableMoves(false)) {
                                        applyMove(m3e);
                                        for (Move m4 : availableMoves(false)) {
                                            applyMove(m4);
                                            printMoves();
                                            undoLastMove();
                                        }
                                        undoLastMove();
                                    }
                                } else {
                                    for (Move m4 : availableMoves(false)) {
                                        applyMove(m4);
                                        printMoves();
                                        if (entangled()) {
                                            for (Move m4e : availableMoves(false)) {
                                                applyMove(m4e);
                                                printMoves();
                                                undoLastMove();
                                            }
                                        }
                                        undoLastMove();
                                    }
                                }
                                undoLastMove();
                            }
                        }
                        undoLastMove();
                    }
                    undoLastMove();
                }
                break;
        }
    }

    private void printMoves() {
        List<Move> moves = minimaxMoves();
        Utility utility = moves.get(0).utility();
        StringBuilder encodedMoves = new StringBuilder(moves.size() * 2);
        for (Move move : moves) {
            if (move.type() == Move.Type.COLLAPSE) {
                encodedMoves.append('0').append(move.firstCellIndex() + 1);
            } else {
                encodedMoves.append(move.firstCellIndex() + 1).append(move.secondCellIndex() + 1);
            }
        }
        output.printf(
                Locale.ROOT,
                "INSERT OR REPLACE INTO opening_book VALUES (%d,%d,%d,'%s');%n",
                openingBookKey(),
                utility.value(),
                utility.depth(),
                encodedMoves);
    }
}
