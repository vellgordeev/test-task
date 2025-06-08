package ru.gordeev.todo.data;

import ru.gordeev.core.data.ModelBuilder;
import ru.gordeev.core.data.PayloadBuilder;
import ru.gordeev.core.data.TestDataRegistry;
import ru.gordeev.todo.model.Todo;

import java.math.BigInteger;
import java.util.Map;

/**
 * Provides valid and invalid test data scenarios for the Todo entity.
 */
public class TodoTestData {

    public static void register(TestDataRegistry registry) {
        registry.register(Todo.class, TodoTestData::defaultTodo);
    }


    /**
     * Creates a default, valid Todo object.
     */
    private static Todo defaultTodo() {
        return Todo.builder()
                .id(ModelBuilder.randomLong(1, Long.MAX_VALUE))
                .text(ModelBuilder.uniqueText("Todo"))
                .completed(ModelBuilder.randomBoolean())
                .build();
    }

    /**
     * Provides payloads for various invalid scenarios.
     */
    public static class Invalid {

        public static Map<String, Object> missingText() {
            return PayloadBuilder.from(defaultTodo())
                    .without("text")
                    .build();
        }

        public static Map<String, Object> nullText() {
            return PayloadBuilder.from(defaultTodo())
                    .with("text", null)
                    .build();
        }

        public static Map<String, Object> invalidIdType() {
            return PayloadBuilder.from(defaultTodo())
                    .with("id", "not-a-number")
                    .build();
        }

        public static Map<String, Object> invalidTextType() {
            return PayloadBuilder.from(defaultTodo())
                    .with("text", 12345)
                    .build();
        }

        public static Map<String, Object> idOverflow() {
            return PayloadBuilder.from(defaultTodo())
                    .with("id", new BigInteger("18446744073709551616"))
                    .build();
        }

        public static Map<String, Object> invalidCompletedType() {
            return PayloadBuilder.from(defaultTodo())
                    .with("completed", "maybe")
                    .build();
        }
    }
}