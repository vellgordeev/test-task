package todo;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.gordeev.core.data.PayloadBuilder;
import ru.gordeev.todo.data.TodoTestData;
import ru.gordeev.todo.model.Todo;

import java.util.Map;

import static io.qameta.allure.Allure.step;
import static ru.gordeev.core.assertions.Assertions.assertThatResponse;
import static ru.gordeev.core.errors.ErrorMessages.*;
import static ru.gordeev.todo.assertions.TodoAssert.assertThat;


@Epic("Backend API Tests")
@Feature("Todo Management")
@Story("Create Todo")
@Owner("Valentin Gordeev")
@Test(groups = {"rest", "crud"})
public class CreateTodoTest extends BaseTodoTest {

    @Test
    @Description("Should create a todo with a standard set of valid data")
    public void shouldCreateTodoWithAllValidFields() {
        Todo todoRequest = step("GIVEN: A valid Todo payload is prepared", () ->
                testData.valid(Todo.class)
        );

        Todo created = step("WHEN: A POST request is sent to create the todo", () ->
                createTodoWithCleanup(todoRequest)
        );

        step("THEN: The created todo is validated against the request", () ->
                assertThat(created).matchesExpected(todoRequest)
        );
    }

    @Test(dataProvider = "validDataVariations")
    @Description("Should correctly create todos with specific boundary values and data variations")
    public void shouldCreateTodoWithValidDataVariations(Todo todoRequest, String caseDescription) {
        step(caseDescription, () -> {
            Todo created = step("WHEN: The todo is created with the specific data variation", () ->
                    createTodoWithCleanup(todoRequest)
            );

            step("THEN: The created todo is validated", () ->
                    assertThat(created).matchesExpected(todoRequest)
            );
        });
    }

    @Test(dataProvider = "malformedPayloads")
    @Description("Should reject requests with malformed payloads (missing fields, wrong types)")
    public void shouldRejectMalformedPayload(Map<String, Object> invalidPayload, String expectedError, String caseDescription) {
        step(caseDescription, () -> {
            Response response = step("WHEN: A request with a malformed payload is sent", () ->
                    todoApi.createRaw(invalidPayload)
            );

            step("THEN: The API responds with 400 Bad Request and a specific error message", () ->
                    assertThatResponse(response)
                            .hasStatusCode(400)
                            .hasBodyContaining(expectedError)
            );
        });
    }

    @Test
    @Description("Should create a todo successfully, ignoring any unrecognized fields in the payload.")
    public void shouldCreateTodoAndIgnoreExtraFields() {
        Todo validBaseTodo = step("GIVEN: A valid base Todo object is prepared", () ->
                testData.valid(Todo.class)
        );

        Map<String, Object> payloadWithExtraField = step("AND: A payload with an extra field is created from the base object", () ->
                PayloadBuilder.from(validBaseTodo)
                        .with("priority", "high")
                        .build()
        );

        step("WHEN: The resource is created using the payload with the extra field", () -> {
            todoApi.createRaw(payloadWithExtraField)
                    .then()
                    .statusCode(201);
        });
        registerTodoForCleanup(validBaseTodo.getId());

        Todo fetchedTodo = step("THEN: The created object can be successfully fetched and deserialized", () ->
                todoApi.getById(validBaseTodo.getId())
        );

        step("AND: The fetched object's data is correct", () ->
                assertThat(fetchedTodo).matchesExpected(validBaseTodo)
        );
    }

    @Test
    @Description("Should reject a todo if the ID is negative")
    public void shouldRejectTodoWithInvalidId() {
        Todo todoRequest = step("GIVEN: A todo payload with a negative ID is prepared", () ->
                testData.validWith(Todo.class, t -> t.setId(-1L))
        );

        Response response = step("WHEN: A POST request is sent with the invalid ID", () ->
                todoApi.createRaw(todoRequest)
        );

        step("THEN: The API responds with 400 Bad Request", () ->
                assertThatResponse(response).hasStatusCode(400)
        );
    }


    @DataProvider(name = "validDataVariations")
    public Object[][] validDataVariations() {
        return new Object[][]{
                {testData.validWith(Todo.class, t -> t.setCompleted(true)), "Case: completed is true"},
                {testData.validWith(Todo.class, t -> t.setCompleted(false)), "Case: completed is false"},
                {testData.validWith(Todo.class, t -> t.setText("")), "Case: text is an empty string"},
                {testData.validWith(Todo.class, t -> t.setText("@@@@@")), "Case: text with symbols"},
                {testData.validWith(Todo.class, t -> t.setText("test'); DROP TABLE todos;")), "Case: text contains SQL injection payload"}
        };
    }

    @DataProvider(name = "malformedPayloads")
    public Object[][] malformedPayloads() {
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
                        TodoTestData.Invalid.idOverflow(),
                        // Interesting conversion
                        invalidType("floating point `1.8446744073709552e19`", "u64"),
                        "Case: 'id' field value causes a u64 overflow"
                },
                {
                        TodoTestData.Invalid.invalidIdType(),
                        invalidType("string \"not-a-number\"", "u64"),
                        "Case: 'id' field has wrong type (String instead of Long)"
                },
                {
                        TodoTestData.Invalid.invalidTextType(),
                        invalidType("integer `12345`", "a string"),
                        "Case: 'text' field has wrong type (Integer instead of String)"
                },
                {
                        TodoTestData.Invalid.invalidCompletedType(),
                        invalidType("string \"maybe\"", "a boolean"),
                        "Case: 'completed' field has wrong type (String instead of Boolean)"
                }
        };
    }
}