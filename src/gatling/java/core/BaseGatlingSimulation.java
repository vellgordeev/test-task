package core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.core.Assertion;
import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.aeonbits.owner.ConfigFactory;
import ru.gordeev.core.config.AppConfig;
import ru.gordeev.todo.api.TodoApiService;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_JSON;

/**
 * An abstract base class for all Gatling simulations, providing a structured
 * framework for building and configuring performance tests. It centralizes
 * protocol configuration, load profile generation, and result assertions.
 */
@Slf4j
public abstract class BaseGatlingSimulation extends Simulation {
    protected final AppConfig config = ConfigFactory.create(AppConfig.class);
    protected final TodoApiService todoApiService;
    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected final HttpProtocolBuilder httpProtocol = http
            .baseUrl(config.baseUri() + ":" + config.basePort())
            .acceptHeader(APPLICATION_JSON)
            .contentTypeHeader(APPLICATION_JSON)
            .userAgentHeader("Gatling Performance Test")
            .shareConnections();

    public BaseGatlingSimulation() {
        var requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBaseUri(config.baseUri())
                .setPort(config.basePort())
                .build();
        this.todoApiService = new TodoApiService(requestSpec, config);
    }


    /**
     * Concrete simulations must implement this method to define the user scenario.
     * @return The ScenarioBuilder that defines the sequence of actions.
     */
    protected abstract ScenarioBuilder defineScenario();

    /**
     * Concrete simulations must implement this method to provide the load profile.
     * @return The LoadProfile configuration for this simulation.
     */
    protected abstract LoadProfile getLoadProfile();

    /**
     * Optional hook for performing cleanup tasks after the simulation completes.
     */
    protected void afterSimulation() {}

    @Override
    public final void before() {
        LoadProfile profile = getLoadProfile();
        log.info("============================================================");
        log.info("Starting performance test: {}", profile.getName());
        log.info("Target: {}:{}", config.baseUri(), config.basePort());
        log.info("Load Pattern: {}", profile.getPattern());
        log.info("============================================================");
    }

    /**
     * Constructs a Gatling PopulationBuilder based on the specified load profile pattern.
     * This method translates the high-level LoadProfile into a concrete Gatling injection strategy.
     */
    protected PopulationBuilder buildPopulation(ScenarioBuilder scenario, LoadProfile profile) {
        final Duration warmupDuration = Duration.ofSeconds(5);

        switch (profile.getPattern()) {
            case CONSTANT_LOAD:
                return scenario.injectOpen(
                        rampUsers(profile.getWarmupRequests()).during(warmupDuration),
                        constantUsersPerSec(profile.getTargetRps())
                                .during(Duration.ofSeconds(profile.getTestDurationSeconds()))
                );

            case RAMP_UP:
                int holdDurationSecs = profile.getTestDurationSeconds() - profile.getRampUpDurationSeconds();
                if (holdDurationSecs < 0) {
                    log.warn("Ramp-up duration is longer than the total test duration. The test will only ramp up.");
                    holdDurationSecs = 0;
                }
                return scenario.injectOpen(
                        rampUsersPerSec(1).to(profile.getTargetRps())
                                .during(Duration.ofSeconds(profile.getRampUpDurationSeconds())),
                        constantUsersPerSec(profile.getTargetRps())
                                .during(Duration.ofSeconds(holdDurationSecs))
                );

            case SPIKE:
                double baseRps = profile.getTargetRps();
                double spikeRps = profile.getSpikeRps() != null ? profile.getSpikeRps() : baseRps * 3;
                int spikeDurationSecs = profile.getSpikeDurationSeconds() != null ? profile.getSpikeDurationSeconds() : 10;
                return scenario.injectOpen(
                        constantUsersPerSec(baseRps).during(Duration.ofSeconds(20)),
                        rampUsersPerSec(baseRps).to(spikeRps).during(Duration.ofSeconds(5)),
                        constantUsersPerSec(spikeRps).during(Duration.ofSeconds(spikeDurationSecs)),
                        rampUsersPerSec(spikeRps).to(baseRps).during(Duration.ofSeconds(5)),
                        constantUsersPerSec(baseRps).during(Duration.ofSeconds(20))
                );

            case STRESS:
                return scenario.injectOpen(
                        rampUsersPerSec(1).to(profile.getTargetRps() * 3)
                                .during(Duration.ofSeconds(profile.getTestDurationSeconds()))
                );

            default:
                throw new IllegalArgumentException("Unsupported load pattern: " + profile.getPattern());
        }
    }

    /**
     * Constructs an array of standard Gatling assertions based on the thresholds
     * defined in the load profile.
     */
    protected Assertion[] buildAssertions(LoadProfile profile) {
        return new Assertion[]{
                global().responseTime().max().lt(profile.getMaxResponseTimeMs()),
                global().responseTime().percentile(95.0).lt(profile.getP95ResponseTimeMs()),
                global().responseTime().percentile(99.0).lt(profile.getP99ResponseTimeMs()),
                global().failedRequests().percent().lt(profile.getMaxErrorRate() * 100)
        };
    }

    @Override
    public final void after() {
        log.info("============================================================");
        log.info("Performance test completed. Running after-simulation hooks.");
        try {
            afterSimulation();
        } catch (Exception e) {
            log.error("After-simulation hook failed!", e);
        }
        log.info("Check the Gatling report for detailed results.");
        log.info("============================================================");
    }
}
