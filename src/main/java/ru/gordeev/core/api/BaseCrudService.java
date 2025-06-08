package ru.gordeev.core.api;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import ru.gordeev.core.config.AppConfig;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

/**
 * Base CRUD implementation.
 */
@Slf4j
public abstract class BaseCrudService<T, ID> extends BaseApiService implements CrudOperations<T, ID> {

    protected final String resourcePath;
    protected final Class<T> entityClass;
    private final String resourceName;

    protected BaseCrudService(RequestSpecification spec, AppConfig config,
                              String resourcePath, Class<T> entityClass) {
        super(spec, config);
        this.resourcePath = resourcePath;
        this.entityClass = entityClass;
        // Extract resource name from path: "/todos" -> "todo"
        this.resourceName = resourcePath.replaceAll("^/", "").replaceAll("s$", "");
    }

    // Checked methods
    @Override
    public T create(T entity) {
        log.debug("Creating {}", entityClass.getSimpleName());
        return createRaw(entity)
                .then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath(getSingleSchema()))
                .extract()
                .as(entityClass);
    }

    @Override
    public T getById(ID id) {
        log.debug("Getting {} by id: {}", entityClass.getSimpleName(), id);
        return getByIdRaw(id)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath(getSingleSchema()))
                .extract()
                .as(entityClass);
    }

    @Override
    public T update(ID id, T entity) {
        log.debug("Updating {} with id: {}", entityClass.getSimpleName(), id);
        return updateRaw(id, entity)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath(getSingleSchema()))
                .extract()
                .as(entityClass);
    }

    @Override
    public void delete(ID id) {
        log.debug("Deleting {} with id: {}", entityClass.getSimpleName(), id);
        deleteRaw(id)
                .then()
                .statusCode(204);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> getAll(Map<String, ?> queryParams) {
        log.debug("Getting all {}", entityClass.getSimpleName());
        return getAllRaw(queryParams)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath(getListSchema()))
                .extract()
                .jsonPath()
                .getList(".", entityClass);
    }

    public List<T> getAll() {
        return getAll(Collections.emptyMap());
    }

    // Raw methods
    @Override
    public Response createRaw(Object payload) {
        return post(resourcePath, payload);
    }

    @Override
    public Response getByIdRaw(ID id) {
        return get(resourcePath + "/" + id);
    }

    @Override
    public Response updateRaw(ID id, Object payload) {
        return put(resourcePath + "/" + id, payload);
    }

    @Override
    public Response deleteRaw(ID id) {
        return delete(resourcePath + "/" + id);
    }

    @Override
    public Response getAllRaw(Map<String, ?> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return get(resourcePath);
        }
        return get(resourcePath, queryParams);
    }

    /**
     * Gets schema path for single entity.
     */
    protected String getSingleSchema() {
        return String.format("schemas/%s-schema.json", resourceName);
    }

    /**
     * Gets schema path for entity list.
     */
    protected String getListSchema() {
        return String.format("schemas/%s-list-schema.json", resourceName);
    }
}