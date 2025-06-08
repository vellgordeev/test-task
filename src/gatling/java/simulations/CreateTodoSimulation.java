package simulations;

import core.BaseGatlingSimulation;
import core.LoadProfile;
import core.PerformanceIdGenerator;
import core.PerformanceProfiles;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.http.HttpRequestActionBuilder;
import ru.gordeev.core.data.TestDataRegistry;
import ru.gordeev.todo.data.TodoTestData;
import ru.gordeev.todo.model.Todo;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

/**
 * A focused performance test specifically for the POST /todos endpoint.
 * This simulation isolates the create operation to measure its performance
 * characteristics under various load patterns, as required by the task.
 */
public class CreateTodoSimulation extends BaseGatlingSimulation {

    public CreateTodoSimulation() {
        LoadProfile profile = getLoadProfile();
        setUp(
                buildPopulation(defineScenario(), profile)
                        .protocols(httpProtocol)
        ).assertions(buildAssertions(profile));
    }

    private String serializeTodo(Object todo) {
        try {
            return objectMapper.writeValueAsString(todo);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object to JSON string", e);
        }
    }

    public LoadProfile getLoadProfile() {
        String profileName = System.getProperty("performance.profile", "stress");
        return switch (profileName.toLowerCase()) {
            case "smoke" -> PerformanceProfiles.smoke();
            case "normal" -> PerformanceProfiles.normal();
            case "spike" -> PerformanceProfiles.spike();
            default -> PerformanceProfiles.stress();
        };
    }

    /**
     * Defines a simple scenario where each virtual user executes a single
     * POST /todos request. This allows for precise measurement of the
     * create operation's performance.
     */
    public ScenarioBuilder defineScenario() {
        TestDataRegistry testData = new TestDataRegistry();
        TodoTestData.register(testData);

        HttpRequestActionBuilder createTodoRequest = http("POST /todos")
                .post("/todos")
                .body(StringBody(session -> {
                    Todo todo = testData.valid(Todo.class);
                    todo.setId(PerformanceIdGenerator.nextId());
                    return serializeTodo(todo);
                }))
                .asJson()
                .check(status().is(201));

        return scenario("Create Todo Endpoint Performance")
                .exec(createTodoRequest);
    }

    @Override
    protected void afterSimulation() {
        todoApiService.deleteAllTodos();
    }
}
