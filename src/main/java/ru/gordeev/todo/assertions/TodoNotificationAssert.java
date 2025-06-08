package ru.gordeev.todo.assertions;

import org.assertj.core.api.AbstractAssert;
import ru.gordeev.todo.model.Todo;
import ru.gordeev.todo.model.TodoNotification;
import ru.gordeev.todo.model.TodoNotification.NotificationType;

public class TodoNotificationAssert extends AbstractAssert<TodoNotificationAssert, TodoNotification> {

    public TodoNotificationAssert(TodoNotification actual) {
        super(actual, TodoNotificationAssert.class);
    }

    public static TodoNotificationAssert assertThat(TodoNotification actual) {
        return new TodoNotificationAssert(actual);
    }

    @Override
    public TodoNotificationAssert isNotNull() {
        super.isNotNull();
        return this;
    }

    /**
     * Verifies notification has expected type.
     */
    public TodoNotificationAssert hasType(NotificationType expected) {
        isNotNull();

        if (!actual.getType().equals(expected)) {
            failWithMessage("Expected notification type to be <%s> but was <%s>",
                    expected, actual.getType());
        }

        return this;
    }

    /**
     * Verifies notification has data matching expected todo.
     */
    public TodoNotificationAssert hasDataMatching(Todo expected) {
        isNotNull();

        if (actual.getData() == null) {
            failWithMessage("Expected notification to have data but it was null");
        }

        TodoNotification.TodoData data = actual.getData();

        if (!data.getId().equals(expected.getId())) {
            failWithMessage("Expected notification data ID to be <%s> but was <%s>",
                    expected.getId(), data.getId());
        }

        if (!data.getText().equals(expected.getText())) {
            failWithMessage("Expected notification data text to be <%s> but was <%s>",
                    expected.getText(), data.getText());
        }

        if (!data.getCompleted().equals(expected.getCompleted())) {
            failWithMessage("Expected notification data completed to be <%s> but was <%s>",
                    expected.getCompleted(), data.getCompleted());
        }

        return this;
    }
}
