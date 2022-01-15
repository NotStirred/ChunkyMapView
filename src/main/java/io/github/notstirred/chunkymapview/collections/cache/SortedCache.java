package io.github.notstirred.chunkymapview.collections.cache;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Cache that sorts when full, and removes the last 20% of elements
 */
@RequiredArgsConstructor
public class SortedCache<K, V> implements SizedCache<K, V> {
    protected final int maxSize;
    protected final Comparator<K> comparator;
    protected final Consumer<V> onRemovalConsumer;

    protected final Map<K, V> cachedPositions = new HashMap<>();
    
    @Override
    public void put(K key, V val) {
        V replaced = this.cachedPositions.put(key, val);
        if(replaced != null)
            this.onRemovalConsumer.accept(replaced);

        if(this.cachedPositions.size() == this.maxSize) {
            trimLastElements();
        }
    }

    @Override
    public synchronized V get(K key) {
        return cachedPositions.get(key);
    }

    @Override
    public synchronized V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        V v = cachedPositions.computeIfAbsent(key, mappingFunction);

        if(this.cachedPositions.size() == this.maxSize) {
            trimLastElements();
        }

        return v;
    }

    @Override
    public synchronized V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> mappingFunction) {
        return cachedPositions.computeIfPresent(key, mappingFunction);
    }

    public synchronized void trimLastElements() {
        @SuppressWarnings("unchecked")
        K[] positions = (K[]) this.cachedPositions.keySet().toArray();
        Arrays.sort(positions, this.comparator);

        for (int i = (int) (positions.length*0.8), l = positions.length; i < l; i++) {
            this.onRemovalConsumer.accept(this.cachedPositions.remove(positions[i]));
        }
    }

    public synchronized Iterable<? extends Map.Entry<K, V>> entrySet() {
        return cachedPositions.entrySet();
    }

    @Override
    public int size() {
        return this.cachedPositions.size();
    }

    @Override
    public int maxSize() {
        return this.maxSize;
    }
}
