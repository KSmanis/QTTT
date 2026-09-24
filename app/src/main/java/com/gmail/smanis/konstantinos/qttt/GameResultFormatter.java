package com.gmail.smanis.konstantinos.qttt;

import android.content.Context;

final class GameResultFormatter {
    private GameResultFormatter() {}

    static String format(Context context, GameResult result) {
        return switch (result) {
            case DRAW -> context.getString(R.string.result_draw);
            case DOUBLE_COMPLETE_WIN -> context.getString(R.string.result_double_winner);
            case NARROW_WIN_FIRST, NARROW_WIN_SECOND ->
                    context.getString(R.string.result_narrow_winner, result.winner());
            case COMPLETE_WIN, LOSS -> context.getString(R.string.result_winner, result.winner());
            case INVALID -> throw new IllegalArgumentException("Game is not over");
        };
    }
}
