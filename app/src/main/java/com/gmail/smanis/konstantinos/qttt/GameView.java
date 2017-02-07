package com.gmail.smanis.konstantinos.qttt;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class GameView extends View {
    public interface OnGameOverListener {
        void onGameOver(GameResult res);
    }
    public interface OnInputListener {
        void onInput(State s);
    }
    //Listeners
    private OnGameOverListener mOnGameOverListener;
    private OnInputListener mOnInputListener;
    //Constants
    private final float cGridLineWidth;
    private final float cGridLinePadding;
    private final float cTextSize;
    private final float cTextPadding;
    private final float cSubscriptSize;
    private final float cSubscriptPadding;
    private final int cGridColor;
    private final int cXColor;
    private final int cOColor;
    private final int cCollapsedColor;
    private final int cInactiveColor;
    private final int cTextWidth;
    private final int cTextHeight;
    //Entanglement Animation
    private final float cMinGlowRadius = 5.f;
    private final float cMaxGlowRadius = 20.f;
    private final float cGlowStep = 1.f;
    private final int cFps = 30;
    private float mGlowRadius;
    private boolean mGlowInc;
    //Game State
    private State mState;
    private List<CellState> mClassicBoardSnapshot;
    private List<EnumSet<CellState>> mQuantumBoardSnapshot;
    private boolean mEntangled;
    private List<Integer> mEntangledCells;
    private boolean mGameOver;
    private List<Integer> mWinningCells;
    //View State
    private Paint mLinePaint, mMarkPaint;
    private float[] mGridLines;
    private RectF[] mGridCells;
    private boolean mDirty, mHistoryShown, mHasInput, mPaused;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mState = new State();

        Resources res = getResources();
        cGridLineWidth = res.getDimension(R.dimen.grid_line_width);
        cGridLinePadding = res.getDimension(R.dimen.grid_line_padding);
        cTextSize = res.getDimension(R.dimen.text_size);
        cTextPadding = res.getDimension(R.dimen.text_padding);
        cSubscriptSize = res.getDimension(R.dimen.subscript_size);
        cSubscriptPadding = res.getDimension(R.dimen.subscript_padding);
        cGridColor = ContextCompat.getColor(context, R.color.grid);
        cXColor = ContextCompat.getColor(context, R.color.x);
        cOColor = ContextCompat.getColor(context, R.color.o);
        cCollapsedColor = ContextCompat.getColor(context, R.color.collapsed);
        cInactiveColor = ContextCompat.getColor(context, R.color.inactive);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(cGridColor);
        mLinePaint.setStrokeWidth(cGridLineWidth);
        mMarkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMarkPaint.setTypeface(Typeface.SERIF);

        mMarkPaint.setTextSize(cTextSize);
        Rect xRect = new Rect();
        mMarkPaint.getTextBounds("X", 0, 1, xRect);
        Rect oRect = new Rect();
        mMarkPaint.getTextBounds("O", 0, 1, oRect);
        cTextWidth = Math.max(xRect.width(), oRect.width());
        cTextHeight = Math.max(xRect.height(), oRect.height());
    }

    private void checkEntanglement() {
        mEntangled = mState.entangled();
        if (mEntangled) {
            mEntangledCells = mState.entangledCells();
            mGlowRadius = 0.f;
            mGlowInc = true;
        } else {
            mEntangledCells = null;
        }
    }
    private void checkGameOver() {
        mGameOver = mState.gameOver();
        if (mGameOver) {
            mWinningCells = mState.winningCells();
            if (mOnGameOverListener != null) {
                mOnGameOverListener.onGameOver(mState.result());
            }
        } else {
            mWinningCells = null;
        }
    }
    private void repaint() {
        if (!mDirty) {
            invalidate();
            mDirty = true;
        }
    }

    public boolean isPaused() {
        return mPaused;
    }
    public void pause() {
        if (mPaused) {
            return;
        }

        mPaused = true;
        mClassicBoardSnapshot = new ArrayList<>(9);
        mQuantumBoardSnapshot = new ArrayList<>(9);
        for (int i = 0; i < 9; ++i) {
            mClassicBoardSnapshot.add(mState.classicBoard().get(i));
            mQuantumBoardSnapshot.add(EnumSet.copyOf(mState.quantumBoard().get(i)));
        }
    }
    public void refresh() {
        checkEntanglement();
        checkGameOver();
        repaint();
    }
    public void resume() {
        if (!mPaused) {
            return;
        }

        mPaused = false;
        mClassicBoardSnapshot = null;
        mQuantumBoardSnapshot = null;
    }
    public void setOnGameOverListener(OnGameOverListener l) {
        mOnGameOverListener = l;
    }
    public void setOnInputListener(OnInputListener l) {
        mOnInputListener = l;
    }
    public State state() {
        return mState;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        List<CellState> classicBoard;
        List<EnumSet<CellState>> quantumBoard;
        if (mPaused) {
            classicBoard = mClassicBoardSnapshot;
            quantumBoard = mQuantumBoardSnapshot;
        } else {
            classicBoard = mState.classicBoard();
            quantumBoard = mState.quantumBoard();
        }

        canvas.drawLines(mGridLines, mLinePaint);
        for (int iGridRow = 0; iGridRow < 3; ++iGridRow) {
            for (int iGridCol = 0; iGridCol < 3; ++iGridCol) {
                int iGridIndex = 3 * iGridRow + iGridCol;
                EnumSet<CellState> qCell = quantumBoard.get(iGridIndex);
                if (qCell.isEmpty()) {
                    continue;
                }

                CellState cCell = classicBoard.get(iGridIndex);
                RectF cellRect = mGridCells[iGridIndex];
                if (cCell != null && !mHistoryShown) {
                    mMarkPaint.setTextSize(3 * cTextSize);
                    Rect textRect = new Rect();
                    mMarkPaint.getTextBounds(cCell.name(), 0, 1, textRect);
                    mMarkPaint.setTextSize(3 * cSubscriptSize);
                    Rect subscriptRect = new Rect();
                    mMarkPaint.getTextBounds(cCell.name(), 1, 2, subscriptRect);
                    float x = cellRect.left + (cellRect.width() - textRect.width() - 3 * cSubscriptPadding - subscriptRect.width()) / 2f;
                    float y = cellRect.top + (cellRect.height() - textRect.height()) / 2f;

                    if (mGameOver && mWinningCells.contains(iGridIndex)) {
                        mMarkPaint.setColor((cCell.ordinal() & 1) == 0 ? cXColor : cOColor);
                        mMarkPaint.setShadowLayer(cMaxGlowRadius, 0, 0, mMarkPaint.getColor());
                    } else {
                        mMarkPaint.setColor(cCollapsedColor);
                    }
                    mMarkPaint.setTextSize(3 * cTextSize);
                    canvas.drawText(cCell.name().substring(0, 1), x, y + textRect.height(), mMarkPaint);
                    mMarkPaint.setTextSize(3 * cSubscriptSize);
                    canvas.drawText(cCell.name().substring(1, 2), x + textRect.width() + 3 * cSubscriptPadding, y + textRect.height(), mMarkPaint);
                    mMarkPaint.clearShadowLayer();
                    continue;
                }

                for (int iCellRow = 0; iCellRow < 3; ++iCellRow) {
                    for (int iCellCol = 0; iCellCol < 3; ++iCellCol) {
                        int iCellIndex = 3 * iCellRow + iCellCol;
                        CellState mark = CellState.values()[iCellIndex];
                        if (!qCell.contains(mark)) {
                            continue;
                        }

                        if (cCell == null) {
                            mMarkPaint.setColor((mark.ordinal() & 1) == 0 ? cXColor : cOColor);
                            if (mEntangled) {
                                if (mEntangledCells.contains(iGridIndex)) {
                                    mMarkPaint.setShadowLayer(mGlowRadius, 0, 0, mMarkPaint.getColor());
                                    if (mark == mState.lastMove().cellState()) {
                                        mMarkPaint.setUnderlineText(true);
                                    }
                                } else {
                                    mMarkPaint.setAlpha(64);
                                }
                            }
                        } else if (cCell == mark) {
                            if (mGameOver && mWinningCells.contains(iGridIndex)) {
                                mMarkPaint.setColor((cCell.ordinal() & 1) == 0 ? cXColor : cOColor);
                                mMarkPaint.setShadowLayer(cMaxGlowRadius, 0, 0, mMarkPaint.getColor());
                            } else {
                                mMarkPaint.setColor(cCollapsedColor);
                            }
                        } else {
                            mMarkPaint.setColor(cInactiveColor);
                        }
                        float x = cellRect.left + iCellCol * cellRect.width() / 3f + (cellRect.width() / 3f - cTextWidth - cTextPadding) / 2f;
                        float y = cellRect.top + iCellRow * cellRect.height() / 3f + (cellRect.height() / 3f - cTextHeight) / 2f;
                        mMarkPaint.setTextSize(cTextSize);
                        canvas.drawText(mark.name().substring(0, 1), x, y + cTextHeight, mMarkPaint);
                        mMarkPaint.setUnderlineText(false);
                        mMarkPaint.clearShadowLayer();
                        mMarkPaint.setTextSize(cSubscriptSize);
                        canvas.drawText(mark.name().substring(1, 2), x + cTextWidth + cSubscriptPadding, y + cTextHeight, mMarkPaint);
                    }
                }
            }
        }

        mDirty = false;
        if (mEntangled) {
            if (mGlowInc) {
                mGlowRadius += cGlowStep;
            } else {
                mGlowRadius -= cGlowStep;
            }
            if (mGlowRadius <= cMinGlowRadius) {
                mGlowInc = true;
            } else if (mGlowRadius >= cMaxGlowRadius) {
                mGlowInc = false;
            }
            postInvalidateDelayed(1000 / cFps);
            mDirty = true;
        }
        if (mHasInput) {
            mHasInput = false;
            if (mOnInputListener != null) {
                mOnInputListener.onInput(mState);
            }
        }
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mGridLines = new float[] {
            w / 3f - cGridLinePadding, 0, w / 3f - cGridLinePadding, h,
            w / 3f + cGridLinePadding, 0, w / 3f + cGridLinePadding, h,
            2 * w / 3f - cGridLinePadding, 0, 2 * w / 3f - cGridLinePadding, h,
            2 * w / 3f + cGridLinePadding, 0, 2 * w / 3f + cGridLinePadding, h,
            0, w / 3f - cGridLinePadding, w, w / 3f - cGridLinePadding,
            0, w / 3f + cGridLinePadding, w, w / 3f + cGridLinePadding,
            0, 2 * w / 3f - cGridLinePadding, w, 2 * w / 3f - cGridLinePadding,
            0, 2 * w / 3f + cGridLinePadding, w, 2 * w / 3f + cGridLinePadding
        };

        mGridCells = new RectF[9];
        for (int iGridRow = 0; iGridRow < 3; ++iGridRow) {
            for (int iGridCol = 0; iGridCol < 3; ++iGridCol) {
                int iGridIndex = 3 * iGridRow + iGridCol;
                mGridCells[iGridIndex] = new RectF();
                mGridCells[iGridIndex].left = iGridCol * w / 3f + (iGridCol == 0 ? 0 : 1) * cGridLinePadding;
                mGridCells[iGridIndex].top = iGridRow * w / 3f + (iGridRow == 0 ? 0 : 1) * cGridLinePadding;
                mGridCells[iGridIndex].right = (iGridCol + 1) * w / 3f - (iGridCol == 2 ? 0 : 1) * cGridLinePadding;
                mGridCells[iGridIndex].bottom = (iGridRow + 1) * w / 3f - (iGridRow == 2 ? 0 : 1) * cGridLinePadding;
            }
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
        case MotionEvent.ACTION_DOWN:
            for (int i = 0; i < 9; ++i) {
                if (mGridCells[i].contains(e.getX(), e.getY())) {
                    if (mState.classicBoard().get(i) != null) {
                        mHistoryShown = !mHistoryShown;
                        repaint();
                    } else if (!mPaused && mState.applyInput(i)) {
                        mHasInput = true;
                        refresh();
                    }
                    break;
                }
            }
            break;
        }
        return super.onTouchEvent(e);
    }
}
