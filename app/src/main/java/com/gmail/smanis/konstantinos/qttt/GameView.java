package com.gmail.smanis.konstantinos.qttt;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.customview.widget.ExploreByTouchHelper;
import java.util.EnumSet;
import java.util.List;

public class GameView extends View {
    public interface OnGameOverListener {
        void onGameOver(GameResult res);
    }

    public interface OnInputListener {
        boolean onInput(int cellIndex);
    }

    // Listeners
    private OnGameOverListener mOnGameOverListener;
    private OnInputListener mOnInputListener;
    // Constants
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
    // Entanglement Animation
    private static final float MIN_GLOW_RADIUS = 5.f;
    private static final float MAX_GLOW_RADIUS = 20.f;
    private static final float GLOW_STEP = 1.f;
    private static final int FPS = 30;
    private float mGlowRadius;
    private boolean mGlowInc;
    // Rendered State
    private State mSnapshot;
    private boolean mEntangled;
    private List<Integer> mEntangledCells;
    private boolean mGameOver;
    private List<Integer> mWinningCells;
    // View State
    private Paint mLinePaint;
    private Paint mMarkPaint;
    private final Rect mTextRect = new Rect();
    private final Rect mSubscriptRect = new Rect();
    private final ExploreByTouchHelper mAccessibilityHelper;
    private RectF[] mGridCells;
    private int mPendingCell = ExploreByTouchHelper.INVALID_ID;
    private boolean mHistoryShown;
    private boolean mPaused;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSnapshot = new State();

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
            setBoundsInScreenFromBoundsInParent(
                    node,
                    new Rect(
                            Math.round(bounds.left), Math.round(bounds.top),
                            Math.round(bounds.right), Math.round(bounds.bottom)));
            node.setContentDescription(cellDescription(virtualViewId));
            boolean enabled =
                    mSnapshot.classicBoard().get(virtualViewId) != null
                            || (!mPaused && !mSnapshot.gameOver());
            node.setEnabled(enabled);
            node.setClickable(enabled);
            if (enabled) {
                node.addAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_CLICK);
            }
        }

        @Override
        protected boolean onPerformActionForVirtualView(
                int virtualViewId, int action, Bundle arguments) {
            if (action != AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_CLICK.getId()
                    || !activateCell(virtualViewId)) {
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
        CellState classicCell = mSnapshot.classicBoard().get(cellIndex);
        String state;
        if (classicCell != null) {
            state = classicCell.name();
        } else if (mSnapshot.quantumBoard().get(cellIndex).isEmpty()) {
            state = getResources().getString(R.string.game_cell_empty);
        } else {
            state = mSnapshot.quantumBoard().get(cellIndex).toString();
        }
        return getResources()
                .getString(
                        R.string.game_cell_description,
                        cellIndex / 3 + 1,
                        cellIndex % 3 + 1,
                        state);
    }

    private boolean activateCell(int cellIndex) {
        if (mSnapshot.classicBoard().get(cellIndex) != null) {
            mHistoryShown = !mHistoryShown;
            invalidate();
            mAccessibilityHelper.invalidateRoot();
        } else if (!mPaused && mOnInputListener != null && mOnInputListener.onInput(cellIndex)) {
            return true;
        } else {
            return false;
        }
        return true;
    }

    private void checkEntanglement() {
        mEntangled = mSnapshot.entangled();
        if (mEntangled) {
            mEntangledCells = mSnapshot.entangledCells();
            mGlowRadius = 0.f;
            mGlowInc = true;
        } else {
            mEntangledCells = null;
        }
    }

    private void checkGameOver() {
        mGameOver = mSnapshot.gameOver();
        if (mGameOver) {
            mWinningCells = mSnapshot.winningCells();
            if (mOnGameOverListener != null) {
                mOnGameOverListener.onGameOver(mSnapshot.result());
            }
        } else {
            mWinningCells = null;
        }
    }

    public boolean isPaused() {
        return mPaused;
    }

    public void render(State state) {
        mSnapshot = state;
        checkEntanglement();
        checkGameOver();
        mAccessibilityHelper.invalidateRoot();
        invalidate();
    }

    public void setPaused(boolean paused) {
        mPaused = paused;
        mAccessibilityHelper.invalidateRoot();
        invalidate();
    }

    public void setOnGameOverListener(OnGameOverListener l) {
        mOnGameOverListener = l;
    }

    public void setOnInputListener(OnInputListener l) {
        mOnInputListener = l;
    }

    int currentTurn() {
        return mSnapshot.currentTurn();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mGridCells == null) {
            return;
        }

        drawGrid(canvas);
        drawCells(canvas);
        animateEntanglement();
    }

    private void drawGrid(Canvas canvas) {
        for (int i = 1; i < 3; ++i) {
            float x = i * getWidth() / 3f;
            canvas.drawLine(x - cGridLinePadding, 0, x - cGridLinePadding, getHeight(), mLinePaint);
            canvas.drawLine(x + cGridLinePadding, 0, x + cGridLinePadding, getHeight(), mLinePaint);
        }
        for (int i = 1; i < 3; ++i) {
            float y = i * getHeight() / 3f;
            canvas.drawLine(0, y - cGridLinePadding, getWidth(), y - cGridLinePadding, mLinePaint);
            canvas.drawLine(0, y + cGridLinePadding, getWidth(), y + cGridLinePadding, mLinePaint);
        }
    }

    private void drawCells(Canvas canvas) {
        List<CellState> classicBoard = mSnapshot.classicBoard();
        List<EnumSet<CellState>> quantumBoard = mSnapshot.quantumBoard();
        for (int cellIndex = 0; cellIndex < quantumBoard.size(); ++cellIndex) {
            EnumSet<CellState> quantumCell = quantumBoard.get(cellIndex);
            if (!quantumCell.isEmpty()) {
                drawCell(canvas, cellIndex, classicBoard.get(cellIndex), quantumCell);
            }
        }
    }

    private void drawCell(
            Canvas canvas, int cellIndex, CellState classicCell, EnumSet<CellState> quantumCell) {
        RectF cellBounds = mGridCells[cellIndex];
        if (classicCell != null && !mHistoryShown) {
            drawCollapsedCell(canvas, cellIndex, classicCell, cellBounds);
            return;
        }

        for (int markIndex = 0; markIndex < CellState.values().length; ++markIndex) {
            CellState mark = CellState.values()[markIndex];
            if (quantumCell.contains(mark)) {
                drawMark(canvas, cellIndex, markIndex, mark, classicCell, cellBounds);
            }
        }
    }

    private void drawCollapsedCell(Canvas canvas, int cellIndex, CellState cell, RectF cellBounds) {
        mMarkPaint.setTextSize(3 * cTextSize);
        mMarkPaint.getTextBounds(cell.name(), 0, 1, mTextRect);
        mMarkPaint.setTextSize(3 * cSubscriptSize);
        mMarkPaint.getTextBounds(cell.name(), 1, 2, mSubscriptRect);
        float x =
                cellBounds.left
                        + (cellBounds.width()
                                        - mTextRect.width()
                                        - 3 * cSubscriptPadding
                                        - mSubscriptRect.width())
                                / 2f;
        float y = cellBounds.top + (cellBounds.height() - mTextRect.height()) / 2f;

        if (mGameOver && mWinningCells.contains(cellIndex)) {
            mMarkPaint.setColor((cell.ordinal() & 1) == 0 ? cXColor : cOColor);
            mMarkPaint.setShadowLayer(MAX_GLOW_RADIUS, 0, 0, mMarkPaint.getColor());
        } else {
            mMarkPaint.setColor(cCollapsedColor);
        }
        mMarkPaint.setTextSize(3 * cTextSize);
        canvas.drawText(cell.name(), 0, 1, x, y + mTextRect.height(), mMarkPaint);
        mMarkPaint.setTextSize(3 * cSubscriptSize);
        canvas.drawText(
                cell.name(),
                1,
                2,
                x + mTextRect.width() + 3 * cSubscriptPadding,
                y + mTextRect.height(),
                mMarkPaint);
        mMarkPaint.clearShadowLayer();
    }

    private void drawMark(
            Canvas canvas,
            int cellIndex,
            int markIndex,
            CellState mark,
            CellState classicCell,
            RectF cellBounds) {
        if (classicCell == null) {
            mMarkPaint.setColor((mark.ordinal() & 1) == 0 ? cXColor : cOColor);
            if (mEntangled) {
                if (mEntangledCells.contains(cellIndex)) {
                    mMarkPaint.setShadowLayer(mGlowRadius, 0, 0, mMarkPaint.getColor());
                    if (mark == mSnapshot.lastMove().cellState()) {
                        mMarkPaint.setUnderlineText(true);
                    }
                } else {
                    mMarkPaint.setAlpha(64);
                }
            }
        } else if (classicCell == mark) {
            if (mGameOver && mWinningCells.contains(cellIndex)) {
                mMarkPaint.setColor((classicCell.ordinal() & 1) == 0 ? cXColor : cOColor);
                mMarkPaint.setShadowLayer(MAX_GLOW_RADIUS, 0, 0, mMarkPaint.getColor());
            } else {
                mMarkPaint.setColor(cCollapsedColor);
            }
        } else {
            mMarkPaint.setColor(cInactiveColor);
        }

        int row = markIndex / 3;
        int column = markIndex % 3;
        float x =
                cellBounds.left
                        + column * cellBounds.width() / 3f
                        + (cellBounds.width() / 3f - cTextWidth - cTextPadding) / 2f;
        float y =
                cellBounds.top
                        + row * cellBounds.height() / 3f
                        + (cellBounds.height() / 3f - cTextHeight) / 2f;
        mMarkPaint.setTextSize(cTextSize);
        canvas.drawText(mark.name(), 0, 1, x, y + cTextHeight, mMarkPaint);
        mMarkPaint.setUnderlineText(false);
        mMarkPaint.clearShadowLayer();
        mMarkPaint.setTextSize(cSubscriptSize);
        canvas.drawText(
                mark.name(), 1, 2, x + cTextWidth + cSubscriptPadding, y + cTextHeight, mMarkPaint);
    }

    private void animateEntanglement() {
        if (mEntangled) {
            if (mGlowInc) {
                mGlowRadius += GLOW_STEP;
            } else {
                mGlowRadius -= GLOW_STEP;
            }
            if (mGlowRadius <= MIN_GLOW_RADIUS) {
                mGlowInc = true;
            } else if (mGlowRadius >= MAX_GLOW_RADIUS) {
                mGlowInc = false;
            }
            postInvalidateDelayed(1000 / FPS);
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

        mGridCells = new RectF[9];
        for (int iGridRow = 0; iGridRow < 3; ++iGridRow) {
            for (int iGridCol = 0; iGridCol < 3; ++iGridCol) {
                int iGridIndex = 3 * iGridRow + iGridCol;
                mGridCells[iGridIndex] = new RectF();
                mGridCells[iGridIndex].left =
                        iGridCol * w / 3f + (iGridCol == 0 ? 0 : 1) * cGridLinePadding;
                mGridCells[iGridIndex].top =
                        iGridRow * w / 3f + (iGridRow == 0 ? 0 : 1) * cGridLinePadding;
                mGridCells[iGridIndex].right =
                        (iGridCol + 1) * w / 3f - (iGridCol == 2 ? 0 : 1) * cGridLinePadding;
                mGridCells[iGridIndex].bottom =
                        (iGridRow + 1) * w / 3f - (iGridRow == 2 ? 0 : 1) * cGridLinePadding;
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
