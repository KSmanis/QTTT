package com.gmail.smanis.konstantinos.qttt;

public enum GameResult {
    DOUBLE_COMPLETE_WIN(2.0f),
    COMPLETE_WIN(1.0f),
    NARROW_WIN_FIRST(1.0f),
    NARROW_WIN_SECOND(0.5f),
    DRAW(0.0f),
    LOSS(0.0f),
    INVALID(0.0f);

    private final float score;

    GameResult(float score) {
        this.score = score;
    }

    public boolean draw() {
        return this == DRAW;
    }

    public boolean gameOver() {
        return this != INVALID;
    }

    public GameResult oResult() {
        switch (this) {
            case DOUBLE_COMPLETE_WIN, COMPLETE_WIN:
                return LOSS;
            case NARROW_WIN_FIRST:
                return NARROW_WIN_SECOND;
            case NARROW_WIN_SECOND:
                return NARROW_WIN_FIRST;
            case LOSS:
                return COMPLETE_WIN;
            case DRAW:
                return DRAW;
            case INVALID:
            default:
                return INVALID;
        }
    }

    public float xScore() {
        return score;
    }

    public float oScore() {
        return oResult().score;
    }

    public Player winner() {
        if (!gameOver() || draw()) {
            return null;
        }

        return (xScore() > oScore() ? Player.X : Player.O);
    }
}
