package com.gmail.smanis.konstantinos.qttt;

public class GameResult {
    enum PlayerResult {
        DOUBLE_COMPLETE_WIN,
        COMPLETE_WIN,
        NARROW_WIN_FIRST,
        NARROW_WIN_SECOND,
        DRAW,
        LOSS,
        INVALID
    }
    private PlayerResult mResultX;

    public GameResult() {
        mResultX = PlayerResult.INVALID;
    }

    private float score(PlayerResult res) {
        switch (res) {
        case DOUBLE_COMPLETE_WIN:
            return 2.0f;
        case COMPLETE_WIN:
        case NARROW_WIN_FIRST:
            return 1.0f;
        case NARROW_WIN_SECOND:
            return 0.5f;
        case DRAW:
        case LOSS:
        case INVALID:
        default:
            return 0.0f;
        }
    }

    public boolean draw() {
        return (mResultX == PlayerResult.DRAW);
    }
    public boolean gameOver() {
        return (mResultX != PlayerResult.INVALID);
    }
    public void setXResult(PlayerResult res) {
        if (res != null) {
            mResultX = res;
        }
    }
    public PlayerResult xResult() {
        return mResultX;
    }
    public PlayerResult oResult() {
        switch (mResultX) {
        case DOUBLE_COMPLETE_WIN:
        case COMPLETE_WIN:
            return PlayerResult.LOSS;
        case NARROW_WIN_FIRST:
            return PlayerResult.NARROW_WIN_SECOND;
        case NARROW_WIN_SECOND:
            return PlayerResult.NARROW_WIN_FIRST;
        case LOSS:
            return PlayerResult.COMPLETE_WIN;
        case DRAW:
            return PlayerResult.DRAW;
        case INVALID:
        default:
            return PlayerResult.INVALID;
        }
    }
    public float xScore() {
        return score(xResult());
    }
    public float oScore() {
        return score(oResult());
    }
    public Player winner() {
        if (!gameOver() || draw()) {
            return null;
        }

        return (xScore() > oScore() ? Player.X : Player.O);
    }

    @Override
    public String toString() {
        if (gameOver()) {
            return String.format("GameResult [Game Over; X: %s; O: %s]", xResult(), oResult());
        } else {
            return "GameResult [Game In Progress]";
        }
    }
}
