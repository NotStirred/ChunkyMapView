package io.github.notstirred.chunkymapview.collections.cache;

public interface SizedCache<K, V> extends Cache<K, V> {
    int maxSize();
}
