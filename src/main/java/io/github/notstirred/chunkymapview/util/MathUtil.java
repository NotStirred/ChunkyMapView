package io.github.notstirred.chunkymapview.util;

public class MathUtil {
    public static int ceilDiv(int a, int b) {
        return -Math.floorDiv(-a, b);
    }

    public static double log2(double N) {
        return (Math.log(N) / Math.log(2));
    }

    public static boolean isPow2(int x) {
        return (x & (x - 1)) == 0;
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

    public static int manhattanDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2) + Math.abs(z1 - z2);
    }

    public static int shortestDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
        return Math.min(Math.min(Math.abs(x1 - x2), Math.abs(y1 - y2)),  Math.abs(z1 - z2));
    }

    public static int manhattanDistance(int x1, int z1, int x2, int z2) {
        return Math.abs(x1 - x2) + Math.abs(z1 - z2);
    }
}
