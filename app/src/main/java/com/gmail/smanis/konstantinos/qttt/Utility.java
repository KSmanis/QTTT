package com.gmail.smanis.konstantinos.qttt;

public record Utility(int value, int depth) {
    public Utility(int value) {
        this(value, -1);
    }

    public String toShortString() {
        return value + "," + depth;
    }

    public static Utility min(Utility a, Utility b) {
        if (a.value != b.value) {
            return (a.value < b.value ? a : b);
        } else {
            return (a.depth >= b.depth ? a : b);
        }
    }

    public static Utility max(Utility a, Utility b) {
        if (a.value != b.value) {
            return (a.value > b.value ? a : b);
        } else {
            return (a.depth >= b.depth ? a : b);
        }
    }

    public static Utility valueOf(String s) {
        String[] fields = s.split(",");
        return new Utility(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]));
    }
}
