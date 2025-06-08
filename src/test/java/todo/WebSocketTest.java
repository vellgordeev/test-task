package todo;

import io.qameta.allure.*;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.gordeev.todo.assertions.TodoNotificationAssert;
import ru.gordeev.todo.model.Todo;
import ru.gordeev.todo.model.TodoNotification;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static io.qameta.allure.Allure.step;
import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static ru.gordeev.core.helpers.PollingUtils.waitForCondition;
import static ru.gordeev.todo.model.TodoNotification.NotificationType.NEW_TODO;

/**
 * Tests for WebSocket real-time notifications functionality.
 * Each test runs with its own WebSocket connection to ensure test independence.
 */
@Slf4j
@Epic("Backend API Tests")
@Feature("Todo WebSocket Notifications")
@Story("Real-time Updates")
@Owner("Valentin Gordeev")
@Test(groups = "websocket")
public class WebSocketTest extends BaseTodoTest {

    private static final long TIMEOUT_LOWER_BOUND_MS = 1900L;
    private static final long TIMEOUT_UPPER_BOUND_MS = 2500L;
    private static final int CONCURRENT_REQUESTS = 10;
    private static final int NOTIFICATION_POLL_TIMEOUT_SECONDS = 2;
    private static final Duration STABILIZATION_TIMEOUT = ofSeconds(2);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(200);

    @BeforeMethod
    public void setupWebSocket() {
        step("Setup WebSocket connection", () -> {
            try {
                todoWebSocket.connect();
                assertThat(todoWebSocket.isConnected())
                        .as("WebSocket should be connected")
                        .isTrue();
                todoWebSocket.resetMetrics();
            } catch (Exception e) {
                fail("Failed to connect WebSocket: " + e.getMessage());
            }
        });
    }

    @AfterMethod(alwaysRun = true)
    public void teardownWebSocket() {
        step("Teardown WebSocket connection", () -> {
            if (todoWebSocket != null && todoWebSocket.isConnected()) {
                todoWebSocket.disconnect();
            }
        });
    }

    @Test
    @Description("Should receive notification when a new todo is created")
    public void shouldReceiveNotificationOnCreate() {
        todoWebSocket.clearNotifications();

        Todo todoRequest = step("GIVEN: A valid Todo payload is prepared", () ->
                testData.valid(Todo.class)
        );

        Todo created = step("WHEN: A new todo is created", () ->
                createTodoWithCleanup(todoRequest)
        );

        step("THEN: A WebSocket notification should be received", () -> {
            TodoNotification notification = waitForNotification(
                    ofSeconds(config.websocketNotificationTimeout())
            );

            TodoNotificationAssert.assertThat(notification)
                    .isNotNull()
                    .hasType(NEW_TODO)
                    .hasDataMatching(created);
        });
    }

    @Test
    @Description("Should handle concurrent todo creation without losing notifications")
    public void shouldHandleConcurrentNotifications() {
        todoWebSocket.clearNotifications();

        step("GIVEN: WebSocket is ready to receive notifications", () -> {
            assertThat(todoWebSocket.isConnected()).isTrue();
            assertThat(todoWebSocket.getQueueSize()).isEqualTo(0);
        });

        List<Todo> createdTodos = step("WHEN: " + CONCURRENT_REQUESTS + " todos are created concurrently", () ->
                IntStream.range(0, CONCURRENT_REQUESTS)
                        .parallel()
                        .mapToObj(i -> createTodoWithCleanup(
                                testData.validWith(Todo.class, t ->
                                        t.setText("Concurrent todo " + i)
                                )
                        ))
                        .toList()
        );

        step("THEN: All notifications should be received", () -> {
            List<TodoNotification> notifications = new ArrayList<>();

            for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
                TodoNotification notification = waitForNotification(
                        ofSeconds(config.websocketNotificationTimeout())
                );
                assertThat(notification)
                        .as("Notification #" + (i + 1) + " should be received")
                        .isNotNull();
                notifications.add(notification);
            }

            List<Long> notificationIds = notifications.stream()
                    .map(n -> n.getData().getId())
                    .toList();

            List<Long> createdIds = createdTodos.stream()
                    .map(Todo::getId)
                    .toList();

            assertThat(notificationIds)
                    .as("All created todos should have notifications")
                    .containsExactlyInAnyOrderElementsOf(createdIds);
        });
    }

    @Test
    @Description("Should not receive notifications for todos created before connection")
    public void shouldNotReceiveOldNotifications() {
        Todo todoBeforeConnection = step("GIVEN: A todo is created before WebSocket connection", () -> {
            todoWebSocket.disconnect();
            assertThat(todoWebSocket.isConnected()).isFalse();
            return createTodoWithCleanup(testData.valid(Todo.class));
        });

        step("WHEN: WebSocket is reconnected", () -> {
            try {
                todoWebSocket.connect();
                assertThat(todoWebSocket.isConnected()).isTrue();

                waitForCondition(
                        () -> {
                            int size = todoWebSocket.getQueueSize();
                            todoWebSocket.clearNotifications();
                            return size;
                        },
                        size -> size == 0,
                        STABILIZATION_TIMEOUT,
                        POLL_INTERVAL
                );
            } catch (Exception e) {
                fail("Failed to reconnect WebSocket: " + e.getMessage());
            }
        });

        Todo todoAfterConnection = step("AND: A new todo is created after connection", () ->
                createTodoWithCleanup(testData.valid(Todo.class))
        );

        step("THEN: Only the new todo notification should be received", () -> {
            TodoNotification notification = waitForNotification(
                    ofSeconds(config.websocketNotificationTimeout())
            );

            TodoNotificationAssert.assertThat(notification)
                    .isNotNull()
                    .hasType(NEW_TODO);

            assertThat(notification.getData().getId())
                    .as("Should receive notification only for todo created after connection")
                    .isEqualTo(todoAfterConnection.getId());

            try {
                TodoNotification unexpectedNotification = todoWebSocket.waitForNotification(
                        NOTIFICATION_POLL_TIMEOUT_SECONDS, TimeUnit.SECONDS
                );
                assertThat(unexpectedNotification)
                        .as("Should not receive notification for todo created before connection")
                        .isNull();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("Interrupted while waiting for notification");
            }
        });
    }

    @Test
    @Description("Should handle notification timeout gracefully without affecting connection")
    public void shouldHandleTimeoutGracefully() {
        todoWebSocket.clearNotifications();

        Long elapsedTime = step("WHEN: Waiting for notification without creating todo", () -> {
            long startTime = System.currentTimeMillis();
            try {
                TodoNotification notification = todoWebSocket.waitForNotification(
                        NOTIFICATION_POLL_TIMEOUT_SECONDS, TimeUnit.SECONDS
                );
                assertThat(notification)
                        .as("Should return null on timeout")
                        .isNull();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("Interrupted while waiting for notification");
            }
            return System.currentTimeMillis() - startTime;
        });

        step("THEN: Timeout should occur within expected time", () -> {
            assertThat(elapsedTime)
                    .as("Should wait approximately the specified timeout")
                    .isBetween(TIMEOUT_LOWER_BOUND_MS, TIMEOUT_UPPER_BOUND_MS);
        });

        step("AND: WebSocket should remain connected", () -> {
            assertThat(todoWebSocket.isConnected())
                    .as("WebSocket should remain connected after timeout")
                    .isTrue();
        });

        step("AND: WebSocket should still work after timeout", () -> {
            Todo todo = createTodoWithCleanup(testData.valid(Todo.class));
            TodoNotification newNotification = waitForNotification(
                    ofSeconds(config.websocketNotificationTimeout())
            );
            TodoNotificationAssert.assertThat(newNotification)
                    .isNotNull()
                    .hasType(NEW_TODO);
        });
    }

    @Test
    @Description("Should track WebSocket metrics for monitoring and debugging")
    public void shouldTrackMetricsCorrectly() {
        todoWebSocket.clearNotifications();
        todoWebSocket.resetMetrics();

        int initialMessages = todoWebSocket.getMessagesReceivedCount();
        int initialErrors = todoWebSocket.getErrorsCount();

        List<Todo> todos = step("WHEN: Multiple todos are created", () ->
                createMultipleTodosWithCleanup(3)
        );

        step("AND: All notifications are consumed", () -> {
            for (int i = 0; i < todos.size(); i++) {
                TodoNotification notification = waitForNotification(
                        ofSeconds(config.websocketNotificationTimeout())
                );
                assertThat(notification).isNotNull();
            }
        });

        step("THEN: Metrics should be updated correctly", () -> {
            int messagesReceived = todoWebSocket.getMessagesReceivedCount() - initialMessages;

            assertThat(messagesReceived)
                    .as("Message counter should match created todos")
                    .isEqualTo(todos.size());

            assertThat(todoWebSocket.getErrorsCount())
                    .as("No errors should occur during normal operation")
                    .isEqualTo(initialErrors);

            assertThat(todoWebSocket.getQueueSize())
                    .as("Queue should be empty after consuming all notifications")
                    .isEqualTo(0);
        });
    }

    private TodoNotification waitForNotification(Duration timeout) {
        return waitForCondition(
                () -> {
                    try {
                        return todoWebSocket.waitForNotification(100, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                },
                Objects::nonNull,
                timeout,
                Duration.ofMillis(100)
        );
    }
}
