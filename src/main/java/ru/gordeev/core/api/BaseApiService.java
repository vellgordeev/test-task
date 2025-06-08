package ru.gordeev.core.api;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import ru.gordeev.core.config.AppConfig;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Base class for all API services.
 */
@Slf4j
public abstract class BaseApiService {

    protected final RequestSpecification spec;
    protected final AppConfig config;

    protected BaseApiService(RequestSpecification spec, AppConfig config) {
        this.spec = spec;
        this.config = config;
    }

    /**
     * GET request.
     */
    protected Response get(String path) {
        log.debug("GET {}", path);
        return given(spec).get(path);
    }

    /**
     * GET request with query parameters.
     */
    protected Response get(String path, Map<String, ?> queryParams) {
        log.debug("GET {} with params: {}", path, queryParams);
        return given(spec)
                .queryParams(queryParams)
                .get(path);
    }

    /**
     * POST request.
     */
    protected Response post(String path, Object body) {
        log.debug("POST {}", path);
        return given(spec)
                .body(body)
                .post(path);
    }

    /**
     * PUT request.
     */
    protected Response put(String path, Object body) {
        log.debug("PUT {}", path);
        return given(spec)
                .body(body)
                .put(path);
    }

    /**
     * DELETE request.
     */
    protected Response delete(String path) {
        log.debug("DELETE {}", path);
        return given(spec).delete(path);
    }

    /**
     * DELETE request with headers.
     */
    protected Response delete(String path, Map<String, String> headers) {
        log.debug("DELETE {} with headers: {}", path, headers);
        return given(spec)
                .headers(headers)
                .delete(path);
    }
}
