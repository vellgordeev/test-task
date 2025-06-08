package ru.gordeev.todo.assertions;

import org.assertj.core.api.AbstractAssert;
import ru.gordeev.todo.model.Todo;

/**
 * Assertions for Todo objects.
 * It will be created using my own generator, if I have enough time ;(
 */
public class TodoAssert extends AbstractAssert<TodoAssert, Todo> {

    public TodoAssert(Todo actual) {
        super(actual, TodoAssert.class);
    }

    public static TodoAssert assertThat(Todo actual) {
        return new TodoAssert(actual);
    }

    public TodoAssert hasId(Long expected) {
        isNotNull();

        if (!actual.getId().equals(expected)) {
            failWithMessage("Expected Todo ID to be <%s> but was <%s>", expected, actual.getId());
        }

        return this;
    }

    public TodoAssert hasText(String expected) {
        isNotNull();

        if (!actual.getText().equals(expected)) {
            failWithMessage("Expected Todo text to be <%s> but was <%s>", expected, actual.getText());
        }

        return this;
    }

    public TodoAssert hasCompleted(Boolean expected) {
        isNotNull();

        if (!actual.getCompleted().equals(expected)) {
            failWithMessage("Expected Todo 'completed' status to be <%s> but was <%s>",
                    expected, actual.getCompleted());
        }

        return this;
    }

    public TodoAssert matchesExpected(Todo expected) {
        isNotNull();
        hasId(expected.getId());
        hasText(expected.getText());
        hasCompleted(expected.getCompleted());
        return this;
    }
}
