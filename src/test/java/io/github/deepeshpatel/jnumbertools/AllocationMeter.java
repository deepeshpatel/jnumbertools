/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools;

import java.lang.management.ManagementFactory;

/**
 * Measures how many bytes the current thread allocates while running an operation.
 * <p>
 * Optimisation tests use it to compare an optimised code path with a reference implementation of the old one.
 * Allocation is a stable thing to assert on, unlike wall-clock time, which varies with machine load. The tests
 * assert a <em>ratio</em> between the two paths and never an absolute byte count, because object sizes differ
 * between JVMs and settings (for example compressed object pointers).
 * </p>
 */
public final class AllocationMeter {

    private static final com.sun.management.ThreadMXBean BEAN = lookup();

    private AllocationMeter() {
    }

    private static com.sun.management.ThreadMXBean lookup() {
        var bean = ManagementFactory.getThreadMXBean();
        if (bean instanceof com.sun.management.ThreadMXBean sun && sun.isThreadAllocatedMemorySupported()) {
            if (!sun.isThreadAllocatedMemoryEnabled()) {
                sun.setThreadAllocatedMemoryEnabled(true);
            }
            return sun;
        }
        return null;
    }

    /** True when this JVM can report per-thread allocation; tests should skip themselves otherwise. */
    public static boolean supported() {
        return BEAN != null;
    }

    /**
     * Average bytes allocated per call of {@code operation}.
     * The operation is first run {@code warmupCalls} times so the JIT has compiled it, then measured over
     * several rounds of {@code calls} calls; the lowest round is returned, which discards rounds disturbed by
     * the JIT compiling in the background.
     */
    public static double bytesPerCall(Runnable operation, int warmupCalls, int calls) {
        long threadId = Thread.currentThread().threadId();
        for (int i = 0; i < warmupCalls; i++) {
            operation.run();
        }
        double lowest = Double.MAX_VALUE;
        for (int round = 0; round < 5; round++) {
            long before = BEAN.getThreadAllocatedBytes(threadId);
            for (int i = 0; i < calls; i++) {
                operation.run();
            }
            long after = BEAN.getThreadAllocatedBytes(threadId);
            lowest = Math.min(lowest, (after - before) / (double) calls);
        }
        return lowest;
    }
}
