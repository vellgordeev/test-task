package core;

import lombok.Builder;
import lombok.Data;

/**
 * Configurable load profile for performance tests.
 * Defines how load is applied during the test.
 */
@Data
@Builder
public class LoadProfile {

    public enum Pattern {
        CONSTANT_LOAD,
        RAMP_UP,
        SPIKE,
        STRESS
    }

    @Builder.Default
    private String name = "Default Load Profile";

    @Builder.Default
    private Pattern pattern = Pattern.CONSTANT_LOAD;

    @Builder.Default
    private int users = 10;

    @Builder.Default
    private int rampUpDurationSeconds = 10;

    @Builder.Default
    private int testDurationSeconds = 60;

    @Builder.Default
    private double targetRps = 10.0;

    @Builder.Default
    private int warmupRequests = 50;

    // for SPIKE pattern
    private Double spikeRps;
    private Integer spikeDurationSeconds;

    // for checks
    @Builder.Default
    private double maxErrorRate = 0.01; // 1%

    @Builder.Default
    private int maxResponseTimeMs = 1000;

    @Builder.Default
    private int p95ResponseTimeMs = 500;

    @Builder.Default
    private int p99ResponseTimeMs = 800;
}
