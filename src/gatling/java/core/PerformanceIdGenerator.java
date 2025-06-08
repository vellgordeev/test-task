package core;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A thread-safe, high-performance generator for creating unique long IDs
 * suitable for performance tests with multiple virtual users. Each thread (virtual user)
 * gets its own independent sequence of IDs to prevent collisions.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class PerformanceIdGenerator {

    private static final long TIME_MULTIPLIER = 1_000_000L;

    private static final ThreadLocal<AtomicLong> THREAD_LOCAL_GENERATOR =
            ThreadLocal.withInitial(() -> {
                long base = System.currentTimeMillis() * TIME_MULTIPLIER;
                long offset = ThreadLocalRandom.current().nextLong(TIME_MULTIPLIER);
                return new AtomicLong(base + offset);
            });


    public static long nextId() {
        return THREAD_LOCAL_GENERATOR.get().incrementAndGet();
    }
}
