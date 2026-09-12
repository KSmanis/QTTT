package com.gmail.smanis.konstantinos.qttt;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.customview.widget.ExploreByTouchHelper;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

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
    private final Rect mTextRect = new Rect();
    private final Rect mSubscriptRect = new Rect();
    private final ExploreByTouchHelper mAccessibilityHelper;
    private float[] mGridLines;
    private RectF[] mGridCells;
    private int mPendingCell = ExploreByTouchHelper.INVALID_ID;
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

        mAccessibilityHelper = new GameAccessibilityHelper();
        ViewCompat.setAccessibilityDelegate(this, mAccessibilityHelper);
    }

    private class GameAccessibilityHelper extends ExploreByTouchHelper {
        GameAccessibilityHelper() {
            super(GameView.this);
        }

        @Override
        protected int getVirtualViewAt(float x, float y) {
            return cellAt(x, y);
        }

        @Override
        protected void getVisibleVirtualViews(List<Integer> virtualViewIds) {
            if (mGridCells != null) {
                for (int i = 0; i < mGridCells.length; ++i) {
                    virtualViewIds.add(i);
                }
            }
        }

        @Override
        protected void onPopulateNodeForVirtualView(
                int virtualViewId, AccessibilityNodeInfoCompat node) {
            RectF bounds = mGridCells[virtualViewId];
            setBoundsInScreenFromBoundsInParent(node, new Rect(
                    Math.round(bounds.left), Math.round(bounds.top),
                    Math.round(bounds.right), Math.round(bounds.bottom)));
            node.setContentDescription(cellDescription(virtualViewId));
            boolean enabled = mState.classicBoard().get(virtualViewId) != null ||
                    (!mPaused && !mState.gameOver());
            node.setEnabled(enabled);
            node.setClickable(enabled);
            if (enabled) {
                node.addAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_CLICK);
            }
        }

        @Override
        protected boolean onPerformActionForVirtualView(
                int virtualViewId, int action, Bundle arguments) {
            if (action != AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_CLICK.getId() ||
                    !activateCell(virtualViewId)) {
                return false;
            }
            sendEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_CLICKED);
            return true;
        }
    }

    private int cellAt(float x, float y) {
        if (mGridCells != null) {
            for (int i = 0; i < mGridCells.length; ++i) {
                if (mGridCells[i].contains(x, y)) {
                    return i;
                }
            }
        }
        return ExploreByTouchHelper.INVALID_ID;
    }

    private CharSequence cellDescription(int cellIndex) {
        CellState classicCell = mState.classicBoard().get(cellIndex);
        String state;
        if (classicCell != null) {
            state = classicCell.name();
        } else if (mState.quantumBoard().get(cellIndex).isEmpty()) {
            state = getResources().getString(R.string.game_cell_empty);
        } else {
            state = mState.quantumBoard().get(cellIndex).toString();
        }
        return getResources().getString(
                R.string.game_cell_description,
                cellIndex / 3 + 1,
                cellIndex % 3 + 1,
                state);
    }

    private boolean activateCell(int cellIndex) {
        if (mState.classicBoard().get(cellIndex) != null) {
            mHistoryShown = !mHistoryShown;
            repaint();
            mAccessibilityHelper.invalidateRoot();
        } else if (!mPaused && mState.applyInput(cellIndex)) {
            mHasInput = true;
            refresh();
        } else {
            return false;
        }
        return true;
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
        mAccessibilityHelper.invalidateRoot();
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
        if (mGridLines == null || mGridCells == null) {
            return;
        }

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
                    mMarkPaint.getTextBounds(cCell.name(), 0, 1, mTextRect);
                    mMarkPaint.setTextSize(3 * cSubscriptSize);
                    mMarkPaint.getTextBounds(cCell.name(), 1, 2, mSubscriptRect);
                    float x = cellRect.left + (cellRect.width() - mTextRect.width() - 3 * cSubscriptPadding - mSubscriptRect.width()) / 2f;
                    float y = cellRect.top + (cellRect.height() - mTextRect.height()) / 2f;

                    if (mGameOver && mWinningCells.contains(iGridIndex)) {
                        mMarkPaint.setColor((cCell.ordinal() & 1) == 0 ? cXColor : cOColor);
                        mMarkPaint.setShadowLayer(cMaxGlowRadius, 0, 0, mMarkPaint.getColor());
                    } else {
                        mMarkPaint.setColor(cCollapsedColor);
                    }
                    mMarkPaint.setTextSize(3 * cTextSize);
                    canvas.drawText(cCell.name().substring(0, 1), x, y + mTextRect.height(), mMarkPaint);
                    mMarkPaint.setTextSize(3 * cSubscriptSize);
                    canvas.drawText(cCell.name().substring(1, 2), x + mTextRect.width() + 3 * cSubscriptPadding, y + mTextRect.height(), mMarkPaint);
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
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(width, height);
        setMeasuredDimension(size, size);
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
        mAccessibilityHelper.invalidateRoot();
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mAccessibilityHelper.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }
    @Override
    public boolean dispatchHoverEvent(MotionEvent event) {
        return mAccessibilityHelper.dispatchHoverEvent(event) || super.dispatchHoverEvent(event);
    }
    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        mAccessibilityHelper.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }
    @Override
    public boolean performClick() {
        boolean handled = super.performClick();
        if (mPendingCell != ExploreByTouchHelper.INVALID_ID) {
            handled |= activateCell(mPendingCell);
        }
        return handled;
    }
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
        case MotionEvent.ACTION_DOWN:
            mPendingCell = cellAt(e.getX(), e.getY());
            if (mPendingCell != ExploreByTouchHelper.INVALID_ID) {
                setPressed(true);
                return true;
            }
            break;
        case MotionEvent.ACTION_UP:
            if (mPendingCell != ExploreByTouchHelper.INVALID_ID) {
                if (mPendingCell == cellAt(e.getX(), e.getY())) {
                    performClick();
                }
                mPendingCell = ExploreByTouchHelper.INVALID_ID;
                setPressed(false);
                return true;
            }
            break;
        case MotionEvent.ACTION_CANCEL:
            mPendingCell = ExploreByTouchHelper.INVALID_ID;
            setPressed(false);
            return true;
        }
        return super.onTouchEvent(e);
    }
}
