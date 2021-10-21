package io.github.notstirred.chunkymapview.tile;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface Cache<K, V> {
    void put(K k, V v);

    V get(K k);

    V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction);
    V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> mappingFunction);

    int size();
}
