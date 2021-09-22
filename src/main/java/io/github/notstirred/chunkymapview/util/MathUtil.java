package io.github.notstirred.chunkymapview.util;

public class MathUtil {
    public static double log2(double N) {
        return (Math.log(N) / Math.log(2));
    }

    public static int nextPow2(int v) {
        v--;
        v |= v >> 1;
        v |= v >> 2;
        v |= v >> 4;
        v |= v >> 8;
        v |= v >> 16;
        v++;
        return v;
    }

    public static int nextMul2(int v) {
        return (v & 1) == 1 ? v + 1 : v;
    }

    public static int prevMul2(int v) {
        return (v & 1) == 1 ? v - 1 : v;
    }

    public static int orThing(int val, int i) {
        while(i >= 0) {
            val |= 1 << i;
            i--;
        }
        return val;
    }
}
