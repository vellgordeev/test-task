package ru.gordeev.core.api;

import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

/**
 * CRUD operations interface.
 */
public interface CrudOperations<T, ID> {

    // Checked methods (with validations)
    T create(T entity);
    T getById(ID id);
    T update(ID id, T entity);
    void delete(ID id);
    List<T> getAll(Map<String, ?> queryParams);

    // Raw methods (flexible)
    Response createRaw(Object payload);
    Response getByIdRaw(ID id);
    Response updateRaw(ID id, Object payload);
    Response deleteRaw(ID id);
    Response getAllRaw(Map<String, ?> queryParams);
}