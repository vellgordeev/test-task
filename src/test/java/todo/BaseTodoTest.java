package todo;

import core.BaseTest;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import ru.gordeev.todo.api.TodoApiService;
import ru.gordeev.todo.api.TodoWebSocketService;
import ru.gordeev.todo.data.TodoTestData;
import ru.gordeev.todo.model.Todo;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static ru.gordeev.core.helpers.CustomAllureListener.withCustomTemplates;


@Slf4j
public abstract class BaseTodoTest extends BaseTest {

    protected TodoApiService todoApi;
    protected TodoWebSocketService todoWebSocket;
    private final Queue<Long> todosToCleanup = new ConcurrentLinkedQueue<>();

    @Override
    protected void configureServices() {
        var requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .addFilter(withCustomTemplates())
                .build();

        var todoService = new TodoApiService(requestSpec, config);
        var webSocketService = new TodoWebSocketService(config);

        api.register(TodoApiService.class, todoService);
        api.register(TodoWebSocketService.class, webSocketService);
    }

    @Override
    protected void configureTestData() {
        TodoTestData.register(testData);
    }

    @BeforeClass(alwaysRun = true)
    @Override
    public void setupApiClient() {
        super.setupApiClient();

        todoApi = api.getService(TodoApiService.class);
        todoWebSocket = api.getService(TodoWebSocketService.class);
    }

    @AfterMethod(alwaysRun = true)
    public void cleanupCreatedTodos() {
        Long id;
        while ((id = todosToCleanup.poll()) != null) {
            try {
                todoApi.deleteRaw(id);
            } catch (Exception e) {
                log.warn("Failed to delete todo {}: {}", id, e.getMessage());
            }
        }
    }

    protected Todo createTodoWithCleanup(Todo todoData) {
        Todo created = todoApi.create(todoData);
        todosToCleanup.add(created.getId());
        log.debug("Created todo with id: {} (will be cleaned up)", created.getId());
        return created;
    }

    protected List<Todo> createMultipleTodosWithCleanup(int count) {
        List<Todo> todos = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Todo todo = createTodoWithCleanup(testData.valid(Todo.class));
            todos.add(todo);
        }
        log.debug("Created {} todos for testing", count);
        return todos;
    }


    protected Todo createTodoWithoutCleanup(Todo todoData) {
        return todoApi.create(todoData);
    }

    protected void registerTodoForCleanup(Long todoId) {
        todosToCleanup.add(todoId);
    }
}