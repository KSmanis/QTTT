package com.gmail.smanis.konstantinos.qttt;

import java.util.Comparator;
import java.util.List;

public class Move {
	static class MinUtilComparator implements Comparator<Move> {
		@Override
		public int compare(Move o1, Move o2) {
			if (o1.mUtility.value() != o2.mUtility.value()) {
                return Integer.valueOf(o1.mUtility.value()).compareTo(o2.mUtility.value());
			} else {
                return Integer.valueOf(o2.mUtility.depth()).compareTo(o1.mUtility.depth());
			}
		}
	}
	static class MaxUtilComparator implements Comparator<Move> {
		@Override
		public int compare(Move o1, Move o2) {
			if (o1.mUtility.value() != o2.mUtility.value()) {
                return Integer.valueOf(o1.mUtility.value()).compareTo(o2.mUtility.value());
			} else {
                return Integer.valueOf(o1.mUtility.depth()).compareTo(o2.mUtility.depth());
			}
		}
	}

    enum Type {
        REGULAR,
        COLLAPSE
    }
	private Type mType;
    private int mCellIndex1, mCellIndex2;
    private CellState mCellState;
    private Utility mUtility;
    private Move mPrevMove;
    private List<Integer> mCollapsedCells;

    public Move(int cellIndex1, int cellIndex2) {
        mType = Type.REGULAR;
        mCellIndex1 = cellIndex1;
        mCellIndex2 = cellIndex2;
    }
	public Move(int cellIndex1, int cellIndex2, CellState cellState) {
		mType = Type.REGULAR;
		mCellIndex1 = cellIndex1;
		mCellIndex2 = cellIndex2;
		mCellState = cellState;
	}
	public Move(int cellIndex1, int cellIndex2, CellState cellState, Utility utility) {
		mType = Type.REGULAR;
		mCellIndex1 = cellIndex1;
		mCellIndex2 = cellIndex2;
		mCellState = cellState;
		mUtility = utility;
	}
    public Move(int cellIndex) {
        mType = Type.COLLAPSE;
        mCellIndex1 = cellIndex;
        mCellIndex2 = -1;
    }
	public Move(int cellIndex, CellState cellState) {
		mType = Type.COLLAPSE;
		mCellIndex1 = cellIndex;
        mCellIndex2 = -1;
		mCellState = cellState;
	}
	public Move(int cellIndex, CellState cellState, Utility utility) {
		mType = Type.COLLAPSE;
		mCellIndex1 = cellIndex;
        mCellIndex2 = -1;
		mCellState = cellState;
		mUtility = utility;
	}

    public Type type() {
        return mType;
    }
    public int firstCellIndex() {
        return mCellIndex1;
    }
    public int secondCellIndex() {
        return mCellIndex2;
    }
    public CellState cellState() {
        return mCellState;
    }
    public Utility utility() {
        return mUtility;
    }
    public Move previousMove() {
        return mPrevMove;
    }
    public List<Integer> collapsedCells() {
        return mCollapsedCells;
    }
    public void setType(Type type) {
        mType = type;
    }
    public void setFirstCellIndex(int firstCellIndex) {
        mCellIndex1 = firstCellIndex;
    }
    public void setSecondCellIndex(int secondCellIndex) {
        mCellIndex2 = secondCellIndex;
    }
    public void setCellState(CellState cs) {
        mCellState = cs;
    }
    public void setUtility(Utility utility) {
        mUtility = utility;
    }
    public void setPreviousMove(Move move) {
        mPrevMove = move;
    }
    public void setCollapsedCells(List<Integer> collapsedCells) {
        mCollapsedCells = collapsedCells;
    }

    public String toShortString() {
        if (mType == Type.REGULAR) {
            return String.format("%d,%d", mCellIndex1, mCellIndex2);
        } else {
            return String.format("%d", mCellIndex1);
        }
    }
	@Override
	public String toString() {
		if (mType == Type.REGULAR) {
			if (mUtility != null) {
				return String.format("Move [%s; %s; %d; %d] -> %s", mType, mCellState, mCellIndex1, mCellIndex2, mUtility);
			} else {
				return String.format("Move [%s; %s; %d; %d]", mType, mCellState, mCellIndex1, mCellIndex2);
			}
		} else {
			if (mUtility != null) {
				return String.format("Move [%s; %s; %d] -> %s", mType, mCellState, mCellIndex1, mUtility);
			} else {
				return String.format("Move [%s; %s; %d]", mType, mCellState, mCellIndex1);
			}
		}
	}

    public static Move valueOf(String s) {
        String[] fields = s.split(",");
        if (fields.length == 2) {
            return new Move(Integer.valueOf(fields[0]), Integer.valueOf(fields[1]));
        } else if (fields.length == 1) {
            return new Move(Integer.valueOf(fields[0]));
        } else {
            return null;
        }
    }
}
