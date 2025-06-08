package ru.gordeev.todo.api;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import ru.gordeev.core.api.BaseCrudService;
import ru.gordeev.core.config.AppConfig;
import ru.gordeev.todo.model.Todo;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import static io.restassured.RestAssured.given;
import static ru.gordeev.core.helpers.AuthTools.encodeBasicAuth;

@Slf4j
public class TodoApiService extends BaseCrudService<Todo, Long> {

    public TodoApiService(RequestSpecification spec, AppConfig config) {
        super(spec, config, "/todos", Todo.class);
    }

    @Override
    public Todo create(Todo entity) {
        Objects.requireNonNull(entity.getId(), "ID cannot be null");
        log.debug("Creating todo with ID: {}", entity.getId());
        createRaw(entity).then().statusCode(201);
        return findByIdInList(entity.getId());
    }

    @Override
    public Response getByIdRaw(Long id) {
        throw new UnsupportedOperationException("No direct GET /todos/{id} endpoint");
    }

    @Override
    public Todo getById(Long id) {
        log.debug("Fetching todo by ID: {}", id);
        return findByIdInList(id);
    }

    @Override
    public Todo update(Long id, Todo entity) {
        log.debug("Updating todo with ID: {}", id);
        updateRaw(id, entity).then().statusCode(200);
        return findByIdInList(id);
    }

    public Response updateRawWithAuth(Long id, Map<String, Object> body) {
        String auth = encodeBasicAuth(config.adminUsername(), config.adminPassword());

        return given(spec)
                .header("Authorization", auth)
                .body(body)
                .put(resourcePath + "/" + id);
    }

    @Override
    public Response deleteRaw(Long id) {
        String auth = encodeBasicAuth(config.adminUsername(), config.adminPassword());
        log.debug("Deleting todo with ID: {} (with auth)", id);
        return delete(resourcePath + "/" + id, Map.of("Authorization", auth));
    }

    public Response deleteRawWithoutAuth(Long id) {
        log.debug("Deleting todo with ID: {} (without auth)", id);
        return delete(resourcePath + "/" + id);
    }

    public void deleteAllTodos() {
        List<Long> todoIds = getAll().stream()
                .map(Todo::getId)
                .toList();

        if (todoIds.isEmpty()) {
            return;
        }
        for (Long id : todoIds) {
            try {
                deleteRaw(id);
            } catch (Exception e) {
                log.warn("An error occurred during cleanup for todo ID {}: {}", id, e.getMessage());
            }
        }
    }

    private Todo findByIdInList(Long id) {
        log.debug("Searching for todo with id {} in the full list.", id);
        return getAll().stream()
                .filter(todo -> todo.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Todo with ID %d not found in the list.".formatted(id)));
    }
}