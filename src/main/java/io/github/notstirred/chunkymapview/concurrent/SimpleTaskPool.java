package io.github.notstirred.chunkymapview.concurrent;

import lombok.NonNull;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;

/**
 * Trivial executor implementation to allow for future chaining to main thread for GL calls
 */
public class SimpleTaskPool implements Executor {
    ConcurrentLinkedDeque<Runnable> tasks = new ConcurrentLinkedDeque<>();

    private final Thread executorThread;

    public SimpleTaskPool(Thread executorThread) {
        this.executorThread = executorThread;
    }

    @Override
    public void execute(@NonNull Runnable runnable) {
        tasks.add(runnable);
    }

    public void runTasks() {
        if(Thread.currentThread() != executorThread)
            throw new IllegalThreadStateException(String.format("runTasks not called from executorThread %s, instead called from %s", executorThread, Thread.currentThread()));

        tasks.removeIf(runnable -> { runnable.run(); return true; });
    }
}
