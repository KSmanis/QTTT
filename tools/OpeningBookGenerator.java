package com.gmail.smanis.konstantinos.qttt;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class OpeningBookGenerator extends State {
    private final PrintStream output;

    private OpeningBookGenerator(PrintStream output) {
        this.output = output;
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: generate-opening-book TURN (0-4)");
        }

        int turn;
        try {
            turn = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("TURN must be between 0 and 4", e);
        }
        if (turn < 0 || turn > 4) {
            throw new IllegalArgumentException("TURN must be between 0 and 4");
        }

        Path outputPath = Path.of(turn + ".txt");
        try (PrintStream output =
                new PrintStream(Files.newOutputStream(outputPath), false, StandardCharsets.UTF_8)) {
            new OpeningBookGenerator(output).generate(turn);
            if (output.checkError()) {
                throw new IOException("Failed to write " + outputPath);
            }
        }
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
        output.printf(
                Locale.ROOT, "%s:%s%n", moveHistory(), moves.get(0).utility().toShortString());
        for (Move move : moves) {
            output.println(move.toShortString());
        }
        output.println();
    }
}
