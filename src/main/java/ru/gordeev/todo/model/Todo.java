package ru.gordeev.todo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.gordeev.core.models.BaseModel;


@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Todo extends BaseModel {
    private Long id;
    private String text;
    private Boolean completed;
}