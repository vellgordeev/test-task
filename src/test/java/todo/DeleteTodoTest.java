package todo;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import ru.gordeev.todo.model.Todo;

import java.util.NoSuchElementException;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static ru.gordeev.core.assertions.Assertions.assertThatResponse;

@Epic("Backend API Tests")
@Feature("Todo Management")
@Story("Delete Todo")
@Owner("Valentin Gordeev")
@Test(groups = {"rest", "crud"})
public class DeleteTodoTest extends BaseTodoTest {

    @Test
    @Description("Should successfully delete an existing todo with valid authorization.")
    public void shouldDeleteExistingTodo() {
        Todo created = step("GIVEN: An existing todo is created", () ->
                createTodoWithoutCleanup(testData.valid(Todo.class))
        );

        step("WHEN: A DELETE request is sent for the created todo's ID", () ->
                todoApi.delete(created.getId())
        );

        step("THEN: The todo can no longer be found in the system", () -> {
            assertThatThrownBy(() -> todoApi.getById(created.getId()))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining("Todo with ID %d not found in the list".formatted(created.getId()));
        });
    }

    @Test
    @Description("Should return 404 Not Found when trying to delete a non-existent todo.")
    public void shouldReturn404WhenDeletingNonExistentTodo() {
        long nonExistentId = step("GIVEN: A non-existent todo ID is defined", () -> 999999L);

        Response response = step("WHEN: A DELETE request is sent for the non-existent ID", () ->
                todoApi.deleteRaw(nonExistentId)
        );

        step("THEN: The API responds with 404 Not Found", () ->
                assertThatResponse(response).hasStatusCode(404)
        );
    }

    @Test
    @Description("Should return 401 Unauthorized when trying to delete without credentials.")
    public void shouldReturn401WhenDeletingWithoutAuth() {
        Todo created = step("GIVEN: An existing todo is created", () ->
                createTodoWithCleanup(testData.valid(Todo.class))
        );

        Response response = step("WHEN: A DELETE request is sent without an Authorization header", () ->
                todoApi.deleteRawWithoutAuth(created.getId())
        );

        step("THEN: The API responds with 401 Unauthorized", () ->
                assertThatResponse(response).hasStatusCode(401)
        );
    }

    @Test
    @Description("Should return 404 Not Found when trying to delete the same todo twice.")
    public void shouldReturn404OnSecondDeleteAttempt() {
        Todo created = step("GIVEN: An existing todo is created", () ->
                createTodoWithoutCleanup(testData.valid(Todo.class))
        );

        step("WHEN: The first DELETE request is sent and succeeds", () ->
                todoApi.delete(created.getId())
        );

        Response secondResponse = step("AND: A second DELETE request is sent for the same ID", () ->
                todoApi.deleteRaw(created.getId())
        );

        step("THEN: The second response is 404 Not Found", () ->
                assertThatResponse(secondResponse).hasStatusCode(404)
        );
    }
}
