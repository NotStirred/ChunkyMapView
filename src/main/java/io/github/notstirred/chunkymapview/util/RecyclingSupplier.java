package io.github.notstirred.chunkymapview.util;

import java.util.ArrayDeque;

public abstract class RecyclingSupplier<T> {
    private final ArrayDeque<T> stack;

    public RecyclingSupplier(int startingCapacity) {
        this.stack = new ArrayDeque<>(startingCapacity);
    }
    public RecyclingSupplier() {
        this.stack = new ArrayDeque<>();
    }

    public T allocate() {
        if(stack.size() > 0)
            return stack.removeFirst();
        return allocate0();
    }

    protected abstract T allocate0();

    public void release(T t) {
        stack.addFirst(t);
    }
}
