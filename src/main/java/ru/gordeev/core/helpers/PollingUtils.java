package ru.gordeev.core.helpers;

import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;

import java.time.Duration;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class PollingUtils {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(3);

    private PollingUtils() {}


    public static <T> T waitForCondition(Supplier<T> supplier, Predicate<T> predicate) {
        return waitForCondition(supplier, predicate, DEFAULT_TIMEOUT, DEFAULT_POLL_INTERVAL);
    }

    public static <T> T waitForCondition(Supplier<T> supplier, Predicate<T> predicate,
                                         Duration timeout, Duration pollInterval) {
        ConditionFactory awaitility = Awaitility.await()
                .atMost(timeout)
                .pollInterval(pollInterval);

        return awaitility.until(supplier::get, predicate);
    }
}
