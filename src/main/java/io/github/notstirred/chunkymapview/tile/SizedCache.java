package io.github.notstirred.chunkymapview.tile;

public interface SizedCache<K, V> extends Cache<K, V> {
    int maxSize();
}
