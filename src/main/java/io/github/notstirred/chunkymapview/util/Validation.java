package io.github.notstirred.chunkymapview.util;

public class Validation {
    public static void check(boolean b, String s) {
        if(!b) {
            throw new IllegalArgumentException(s);
        }
    }
}
