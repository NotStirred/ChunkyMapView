package io.github.notstirred.chunkymapview.track;

public interface View<POS> {
    boolean hasLevel(int level);
    boolean contains(POS tilePos);
}
