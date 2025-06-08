package core;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * A factory class that provides a set of predefined, reusable load profiles.
 * Each profile represents a standard performance testing scenario (e.g., Smoke, Stress)
 * with well-defined load patterns and assertion thresholds.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class PerformanceProfiles {

    /**
     * A quick, minimal load test to verify basic system stability and functionality.
     * <p>
     * - **Pattern:** Constant low-level load.
     * - **Duration:** 1 minute.
     * - **RPS:** 2 requests per second.
     */
    public static LoadProfile smoke() {
        return LoadProfile.builder()
                .name("Smoke Test - Minimal Load")
                .pattern(LoadProfile.Pattern.CONSTANT_LOAD)
                .testDurationSeconds(60)
                .targetRps(2.0)
                .warmupRequests(10)
                .maxResponseTimeMs(2000)
                .p95ResponseTimeMs(1000)
                .p99ResponseTimeMs(1500)
                .build();
    }

    /**
     * A sustained load test that simulates typical, average daily usage.
     * <p>
     * - **Pattern:** Gradual ramp-up to a steady state.
     * - **Duration:** 5 minutes.
     * - **RPS:** 10 requests per second.
     */
    public static LoadProfile normal() {
        return LoadProfile.builder()
                .name("Normal Load Test")
                .pattern(LoadProfile.Pattern.RAMP_UP)
                .rampUpDurationSeconds(60)
                .testDurationSeconds(300)
                .targetRps(10.0)
                .warmupRequests(50)
                .maxResponseTimeMs(1000)
                .p95ResponseTimeMs(500)
                .p99ResponseTimeMs(800)
                .build();
    }

    /**
     * An aggressive test designed to push the system to its limits and find its breaking point.
     * <p>
     * - **Pattern:** Continuously increasing load.
     * - **Duration:** 3 minutes.
     * - **RPS:** Ramps up to 300 requests per second.
     */
    public static LoadProfile stress() {
        return LoadProfile.builder()
                .name("Stress Test - Finding Limits")
                .pattern(LoadProfile.Pattern.STRESS)
                .testDurationSeconds(180)
                .targetRps(100.0)
                .maxErrorRate(0.05) // Allow 5% errors
                .maxResponseTimeMs(5000)
                .p95ResponseTimeMs(2000)
                .p99ResponseTimeMs(3000)
                .build();
    }

    /**
     * A test that simulates a sudden, massive burst of traffic on top of a normal load.
     * <p>
     * - **Pattern:** Steady state, followed by a sharp spike, then a return to normal.
     * - **Base RPS:** 10 requests per second.
     * - **Spike RPS:** 50 requests per second.
     */
    public static LoadProfile spike() {
        return LoadProfile.builder()
                .name("Spike Test - Traffic Burst")
                .pattern(LoadProfile.Pattern.SPIKE)
                .targetRps(10.0)
                .spikeRps(50.0)
                .spikeDurationSeconds(20)
                .maxErrorRate(0.02) // Allow 2% errors
                .maxResponseTimeMs(2000)
                .p95ResponseTimeMs(1000)
                .p99ResponseTimeMs(1500)
                .build();
    }
}