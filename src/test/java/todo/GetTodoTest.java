package todo;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.gordeev.todo.model.Todo;

import java.util.List;
import java.util.Map;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static ru.gordeev.core.assertions.Assertions.assertThatResponse;
import static ru.gordeev.core.errors.ErrorMessages.INVALID_QUERY_STRING;

@Epic("Backend API Tests")
@Feature("Todo Management")
@Story("Get Todos")
@Owner("Valentin Gordeev")
@Test(groups = {"rest", "crud", "sequential"})
public class GetTodoTest extends BaseTodoTest {

    @Test
    @Description("Should retrieve a complete list of all existing todos.")
    public void shouldGetAllExistingTodos() {
        List<Todo> createdTodos = step("GIVEN: A set of 3 todos is created", () ->
                createMultipleTodosWithCleanup(3)
        );

        List<Todo> receivedTodos = step("WHEN: A GET request is sent to /todos", () ->
                todoApi.getAll()
        );

        step("THEN: The received list contains all the created todos", () ->
                assertThat(receivedTodos).containsAll(createdTodos)
        );
    }

    @Test
    @Description("Should correctly return a paginated list using 'limit' and 'offset'.")
    public void shouldReturnPaginatedResults() {
        List<Todo> created = step("GIVEN: A list of 5 todos is created", () ->
                createMultipleTodosWithCleanup(5)
        );

        List<Todo> paginatedList = step("WHEN: A GET request with limit=2 and offset=1 is sent", () ->
                todoApi.getAll(Map.of("limit", 2, "offset", 1))
        );

        step("THEN: The response contains exactly 2 todos, skipping the first one", () -> {
            assertThat(paginatedList).hasSize(2);
            assertThat(paginatedList).extracting(Todo::getId)
                    .containsExactlyInAnyOrder(created.get(1).getId(), created.get(2).getId());
        });
    }

    @Test
    @Description("Should return an empty list when no todos exist in the system.")
    public void shouldReturnEmptyListWhenNoTodosExist() {
        step("GIVEN: Clean the system state", () -> {
            todoApi.deleteAllTodos();
        });

        List<Todo> receivedTodos = step("WHEN: A GET request is sent to /todos", () ->
                todoApi.getAll()
        );

        step("THEN: The response is an empty, non-null list", () ->
                assertThat(receivedTodos).isNotNull().isEmpty()
        );
    }

    @Test
    @Description("Should return an empty list when the offset is greater than or equal to the total number of todos.")
    public void shouldReturnEmptyListWhenOffsetIsTooLarge() {
        int totalTodos = step("GIVEN: A set of 3 todos is created", () ->
                createMultipleTodosWithCleanup(3).size()
        );

        List<Todo> receivedTodos = step("WHEN: A GET request with an offset equal to the total is sent", () ->
                todoApi.getAll(Map.of("offset", totalTodos))
        );

        step("THEN: The response is an empty list", () ->
                assertThat(receivedTodos).isEmpty()
        );
    }

    @Test(dataProvider = "invalidPaginationParams")
    @Description("Should handle invalid pagination parameters gracefully without crashing.")
    public void shouldHandleInvalidPaginationParams(Map<String, Object> params, String expectedError, String caseDescription) {
        step(caseDescription, () -> {
            step("GIVEN: At least one todo exists in the system", () ->
                    createMultipleTodosWithCleanup(1)
            );

            Response response = step("WHEN: A GET request with invalid parameters is sent", () ->
                    todoApi.getAllRaw(params)
            );

            step("THEN: The API responds with 400 Bad Request and a specific error message", () -> {
                assertThatResponse(response)
                        .hasStatusCode(400)
                        .hasBodyContaining(expectedError);
            });
        });
    }

    @DataProvider(name = "invalidPaginationParams", parallel = true)
    public Object[][] invalidPaginationParams() {
        return new Object[][]{
                {
                        Map.of("limit", -1),
                        INVALID_QUERY_STRING,
                        "Case: limit is negative"
                },
                {
                        Map.of("offset", -1),
                        INVALID_QUERY_STRING,
                        "Case: offset is negative"
                },
                {
                        Map.of("limit", "abc"),
                        INVALID_QUERY_STRING,
                        "Case: limit is not a number"
                },
                {
                        Map.of("offset", "xyz"),
                        INVALID_QUERY_STRING,
                        "Case: offset is not a number"
                }
        };
    }
}
