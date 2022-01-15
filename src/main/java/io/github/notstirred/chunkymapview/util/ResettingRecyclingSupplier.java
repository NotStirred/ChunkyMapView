package io.github.notstirred.chunkymapview.util;

import java.util.ArrayDeque;
import java.util.function.Supplier;

public abstract class ResettingRecyclingSupplier<T extends ResettingRecyclingSupplier.Recyclable> extends RecyclingSupplier<T> {
    private final ArrayDeque<T> stack;

    public ResettingRecyclingSupplier(int startingCapacity) {
        this.stack = new ArrayDeque<>(startingCapacity);
    }
    public ResettingRecyclingSupplier() {
        this.stack = new ArrayDeque<>();
    }

    @Override
    public T allocate() {
        if(stack.size() > 0)
            return stack.removeFirst();
        return allocate0();
    }

    @Override
    protected abstract T allocate0();

    @Override
    public void release(T t) {
        t.reset();
        stack.addFirst(t);
    }

    public interface Recyclable {
        void reset();
    }

    public static class SimpleResettingRecyclingSupplier<T> extends RecyclingSupplier<T> {
        private final Supplier<T> supplier;

        public SimpleResettingRecyclingSupplier(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        protected T allocate0() {
            return supplier.get();
        }
    }
}
