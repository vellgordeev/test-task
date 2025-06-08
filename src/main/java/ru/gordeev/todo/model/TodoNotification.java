package ru.gordeev.todo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.gordeev.core.models.BaseModel;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TodoNotification extends BaseModel {
    private NotificationType  type;
    private TodoData data;

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TodoData {
        private Long id;
        private String text;
        private Boolean completed;
    }

    @Getter
    @RequiredArgsConstructor
    public enum NotificationType {
        NEW_TODO("new_todo");

        @JsonValue
        private final String value;
    }
}
