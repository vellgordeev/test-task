package simulations;

import core.BaseGatlingSimulation;
import core.LoadProfile;
import core.PerformanceIdGenerator;
import core.PerformanceProfiles;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.http.HttpRequestActionBuilder;
import ru.gordeev.core.data.TestDataRegistry;
import ru.gordeev.core.helpers.AuthTools;
import ru.gordeev.todo.data.TodoTestData;
import ru.gordeev.todo.model.Todo;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

/**
 * Simulates a realistic end-to-end user journey for the Todo API.
 * Each virtual user performs a complete workflow: creating a new todo,
 * verifying its existence, optionally updating it, and finally deleting it.
 */
public class TodoUserJourneySimulation extends BaseGatlingSimulation {
    private final TestDataRegistry testData = createTestDataRegistry();
    private final String authHeader = AuthTools.encodeBasicAuth(
            config.adminUsername(),
            config.adminPassword()
    );

    private static TestDataRegistry createTestDataRegistry() {
        TestDataRegistry registry = new TestDataRegistry();
        TodoTestData.register(registry);
        return registry;
    }

    {
        setUp(
                buildPopulation(defineScenario(), getLoadProfile())
                        .protocols(httpProtocol)
        ).assertions(buildAssertions(getLoadProfile()));
    }

    private String serializeTodo(Object todo) {
        try {
            return objectMapper.writeValueAsString(todo);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object to JSON string", e);
        }
    }

    /**
     * Defines a realistic user workflow that simulates the complete lifecycle of a Todo item.
     * The scenario includes think time (pauses) and decision-making (randomSwitch)
     * to mimic real user behavior more closely.
     */
    @Override
    protected ScenarioBuilder defineScenario() {
        HttpRequestActionBuilder createTodoRequest = http("1. Create Todo")
                .post("/todos")
                .body(StringBody("#{todoJsonPayload}"))
                .asJson()
                .check(status().is(201));

        HttpRequestActionBuilder verifyCreationRequest = http("2. Verify Todo Creation")
                .get("/todos")
                .check(status().is(200))
                .check(jsonPath("$[?(@.id == #{todoId})].id").exists());

        HttpRequestActionBuilder updateTodoRequest = http("3. Update Todo")
                .put("/todos/#{todoId}")
                .body(StringBody(session -> {
                    long id = session.getLong("todoId");
                    String text = session.getString("todoText");
                    Todo updated = new Todo(id, text, true);
                    return serializeTodo(updated);
                }))
                .asJson()
                .check(status().is(200));

        HttpRequestActionBuilder deleteTodoRequest = http("4. Delete Todo")
                .delete("/todos/#{todoId}")
                .header("Authorization", authHeader)
                .check(status().is(204));

        return scenario("Todo User Journey")
                .exec(session -> {
                    Todo todo = testData.valid(Todo.class);
                    todo.setId(PerformanceIdGenerator.nextId());
                    return session
                            .set("todoId", todo.getId())
                            .set("todoText", todo.getText())
                            .set("todoJsonPayload", serializeTodo(todo));
                })
                .exec(createTodoRequest)
                .pause(Duration.ofMillis(500), Duration.ofSeconds(1))
                .exec(verifyCreationRequest)
                .pause(Duration.ofSeconds(1), Duration.ofSeconds(3))
                .randomSwitch().on(
                        percent(70.0).then(exec(updateTodoRequest))
                )
                .pause(Duration.ofSeconds(1), Duration.ofSeconds(2))
                .exec(deleteTodoRequest);
    }

    @Override
    protected LoadProfile getLoadProfile() {
        String profileName = System.getProperty("performance.profile", config.performanceProfile());
        return switch (profileName.toLowerCase()) {
            case "smoke" -> PerformanceProfiles.smoke();
            case "stress" -> PerformanceProfiles.stress();
            case "spike" -> PerformanceProfiles.spike();
            default -> PerformanceProfiles.normal();
        };
    }
}
