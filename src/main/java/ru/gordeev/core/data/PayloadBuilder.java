package ru.gordeev.core.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * A generic and fluent builder for creating Map-based payloads for API requests.
 */
public class PayloadBuilder {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final Map<String, Object> data;

    private PayloadBuilder(Map<String, Object> initialData) {
        this.data = new HashMap<>(initialData);
    }

    /**
     * Starts a new, empty payload builder.
     */
    public static PayloadBuilder builder() {
        return new PayloadBuilder(new HashMap<>());
    }

    /**
     * Initializes the builder with the fields from an existing POJO.
     * This is the recommended way to create variations of valid objects.
     *
     * @param pojo The object to use as a base.
     * @return A new PayloadBuilder instance pre-filled with data from the pojo.
     */
    public static PayloadBuilder from(Object pojo) {
        Map<String, Object> pojoAsMap = MAPPER.convertValue(pojo, new TypeReference<>() {});
        return new PayloadBuilder(pojoAsMap);
    }

    /**
     * Sets or overwrites a field with a specific value.
     */
    public PayloadBuilder with(String field, Object value) {
        this.data.put(field, value);
        return this;
    }

    /**
     * Removes one or more fields from the payload.
     */
    public PayloadBuilder without(String... fields) {
        for (String field : fields) {
            this.data.remove(field);
        }
        return this;
    }

    /**
     * Builds the final payload map.
     * @return A copy of the configured payload map.
     */
    public Map<String, Object> build() {
        return new HashMap<>(this.data);
    }
}
