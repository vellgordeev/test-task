package todo;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.gordeev.todo.data.TodoTestData;
import ru.gordeev.todo.model.Todo;

import java.util.Map;

import static io.qameta.allure.Allure.step;
import static ru.gordeev.core.assertions.Assertions.assertThatResponse;
import static ru.gordeev.core.errors.ErrorMessages.*;
import static ru.gordeev.todo.assertions.TodoAssert.assertThat;

@Epic("Backend API Tests")
@Feature("Todo Management")
@Story("Update Todo")
@Owner("Valentin Gordeev")
@Test(groups = {"rest", "crud"})
public class UpdateTodoTest extends BaseTodoTest {

    @Test
    @Description("Should successfully update all fields of an existing todo with a full payload.")
    public void shouldFullyReplaceExistingTodo() {
        Todo initialTodo = step("GIVEN: An existing todo is created", () ->
                createTodoWithCleanup(testData.valid(Todo.class))
        );
        Todo updateRequest = step("AND: An update payload with all new values is prepared", () ->
                new Todo(initialTodo.getId(), "This is a completely new text.", true)
        );
        Todo updatedTodo = step("WHEN: A PUT request is sent with the new payload", () ->
                todoApi.update(initialTodo.getId(), updateRequest)
        );
        step("THEN: All fields of the todo are updated to the new values", () ->
                assertThat(updatedTodo).matchesExpected(updateRequest)
        );
    }

    // That case seems like not specification behaviour (auth is not need here), should be researched more.
    @Test
    @Description("Should return 400 Bad Request for a partial payload when authenticated.")
    public void shouldRejectPartialPayloadWhenAuthenticated() {
        Todo created = step("GIVEN: An existing todo is created", () ->
                createTodoWithCleanup(testData.valid(Todo.class))
        );
        Map<String, Object> partialPayload = step("AND: A partial payload missing the 'completed' field is prepared", () ->
                Map.of("id", created.getId(), "text", "Partial update text")
        );
        Response response = step("WHEN: An authenticated PUT request is sent with the partial payload", () ->
                todoApi.updateRawWithAuth(created.getId(), partialPayload)
        );
        step("THEN: The API responds with 400 and a 'missing field' error", () ->
                assertThatResponse(response)
                        .hasStatusCode(400)
                        .hasBodyContaining(missingField("completed"))
        );
    }

    // These cases seem to be non-standard behavior, I suggest creating a bug report.
    @Test(dataProvider = "malformedUpdatePayloads")
    @Description("Should reject update requests with various malformed payloads.")
    @Link(name = "BUG-1: Invalid payload validation is skipped for unauthenticated PUT requests", url = "https://jira.example.com/browse/BUG-451")
    public void shouldRejectMalformedUpdatePayload(Map<String, Object> invalidPayload, String expectedError, String caseDescription) {
        step(caseDescription, () -> {
            Todo created = step("GIVEN: An existing todo is created", () ->
                    createTodoWithCleanup(testData.valid(Todo.class))
            );
            Response response = step("WHEN: A PUT request with a malformed payload is sent", () ->
                    todoApi.updateRaw(created.getId(), invalidPayload)
            );

            // It must return 400 bad request instead of 401.
            step("THEN: The API responds with 400 Bad Request and a specific error message", () ->
                    assertThatResponse(response)
                            .hasStatusCode(400)
                            .hasBodyContaining(expectedError)
            );
        });
    }

    @Test
    @Description("Should return 404 Not Found when trying to update a non-existent todo.")
    public void shouldReturn404WhenUpdatingNonExistentTodo() {
        long nonExistentId = step("GIVEN: A non-existent todo ID is defined", () -> 999999L);
        Todo validPayload = step("AND: A valid request payload is prepared", () -> testData.valid(Todo.class));
        Response response = step("WHEN: A PUT request is sent for the non-existent ID", () ->
                todoApi.updateRaw(nonExistentId, validPayload)
        );
        step("THEN: The API responds with 404 Not Found", () ->
                assertThatResponse(response).hasStatusCode(404)
        );
    }


    @DataProvider(name = "malformedUpdatePayloads")
    public Object[][] malformedUpdatePayloads() {
        return new Object[][]{
                {
                        TodoTestData.Invalid.missingText(),
                        missingField("text"),
                        "Case: Missing 'text' field"
                },
                {
                        TodoTestData.Invalid.nullText(),
                        NULL_NOT_A_STRING,
                        "Case: 'text' field is null"
                },
                {
                        TodoTestData.Invalid.invalidCompletedType(),
                        invalidType("string \"maybe\"", "a boolean"),
                        "Case: 'completed' has wrong type"
                },
                {
                        TodoTestData.Invalid.idOverflow(),
                        invalidType("floating point `1.8446744073709552e19`", "u64"),
                        "Case: 'id' field value causes a u64 overflow"
                }
        };
    }
}
