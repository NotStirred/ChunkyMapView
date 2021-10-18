package io.github.notstirred.chunkymapview.tile;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Cache that sorts when full, and removes the last 20% of elements
 */
@RequiredArgsConstructor
public class SortedCache<K, V> {
    private final int maxSize;
    private final Comparator<K> comparator;
    
    private final Map<K, V> cachedPositions = new HashMap<>();
    
    public void put(K key, V val) {
        this.cachedPositions.put(key, val);

        if(this.cachedPositions.size() == this.maxSize) {
            trimLastElements();
        }
    }

    public synchronized V get(K key) {
        return cachedPositions.get(key);
    }

    public synchronized V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        V v = cachedPositions.computeIfAbsent(key, mappingFunction);

        if(this.cachedPositions.size() == this.maxSize) {
            trimLastElements();
        }

        return v;
    }

    public synchronized V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> mappingFunction) {
        return cachedPositions.computeIfPresent(key, mappingFunction);
    }

    public synchronized void trimLastElements() {
        @SuppressWarnings("unchecked")
        K[] positions = (K[]) this.cachedPositions.keySet().toArray();
        Arrays.sort(positions, this.comparator);

        for (int i = (int) (positions.length*0.8), l = positions.length; i < l; i++) {
            this.cachedPositions.remove(positions[i]);
        }
    }

    public synchronized Iterable<? extends Map.Entry<K, V>> entrySet() {
        return cachedPositions.entrySet();
    }
}
