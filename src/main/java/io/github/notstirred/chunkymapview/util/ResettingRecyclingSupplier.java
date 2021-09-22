package io.github.notstirred.chunkymapview.util;

import java.util.ArrayDeque;

public abstract class ResettingRecyclingSupplier<T extends ResettingRecyclingSupplier.Recyclable> {
    private final ArrayDeque<T> stack;

    public ResettingRecyclingSupplier(int startingCapacity) {
        this.stack = new ArrayDeque<>(startingCapacity);
    }
    public ResettingRecyclingSupplier() {
        this.stack = new ArrayDeque<>();
    }

    public T allocate() {
        if(stack.size() > 0)
            return stack.removeFirst();
        return allocate0();
    }

    protected abstract T allocate0();

    public void release(T t) {
        t.reset();
        stack.addFirst(t);
    }

    public interface Recyclable {
        void reset();
    }
}
