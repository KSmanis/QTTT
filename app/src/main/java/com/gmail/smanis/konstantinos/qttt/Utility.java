package com.gmail.smanis.konstantinos.qttt;

public class Utility {
	private int mValue;
	private int mDepth;

	public Utility(int value) {
		mValue = value;
		mDepth = -1;
	}
	public Utility(int value, int depth) {
		mValue = value;
		mDepth = depth;
	}

	public int value() {
		return mValue;
	}
	public int depth() {
		return mDepth;
	}
	public void setValue(int value) {
		mValue = value;
	}
	public void setDepth(int depth) {
		mDepth = depth;
	}

    public String toShortString() {
        return String.format("%d,%d", mValue, mDepth);
    }
	@Override
    public String toString() {
    	return String.format("Utility [Value: %d; Depth: %d]", mValue, mDepth);
    }

    public static Utility min(Utility a, Utility b) {
    	if (a.mValue != b.mValue) {
    		return (a.mValue < b.mValue ? a : b);
    	} else {
    		return (a.mDepth >= b.mDepth ? a : b);
    	}
    }
    public static Utility max(Utility a, Utility b) {
    	if (a.mValue != b.mValue) {
    		return (a.mValue > b.mValue ? a : b);
    	} else {
    		return (a.mDepth >= b.mDepth ? a : b);
    	}
    }
	public static Utility valueOf(String s) {
        String[] fields = s.split(",");
        return new Utility(Integer.valueOf(fields[0]), Integer.valueOf(fields[1]));
    }
}
