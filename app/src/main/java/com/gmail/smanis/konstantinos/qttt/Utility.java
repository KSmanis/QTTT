package com.gmail.smanis.konstantinos.qttt;

import java.util.Locale;

public class Utility {
    private final int mValue;
    private final int mDepth;

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

    public String toShortString() {
        return String.format(Locale.ROOT, "%d,%d", mValue, mDepth);
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "Utility [Value: %d; Depth: %d]", mValue, mDepth);
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
        return new Utility(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]));
    }
}
