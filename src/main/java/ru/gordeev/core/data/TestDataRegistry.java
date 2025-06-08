package ru.gordeev.core.data;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Central registry for all test data builders.
 * Provides unified access to model builders.
 */
@Slf4j
public class TestDataRegistry {

    private final Map<Class<?>, ModelBuilder<?>> builders = new HashMap<>();

    /**
     * Registers builder for model class.
     */
    public <T> void register(Class<T> modelClass, Supplier<T> defaultSupplier) {
        log.debug("Registering builder for: {}", modelClass.getSimpleName());
        builders.put(modelClass, ModelBuilder.of(defaultSupplier));
    }

    /**
     * Gets builder for model class.
     */
    @SuppressWarnings("unchecked")
    public <T> ModelBuilder<T> getBuilderFor(Class<T> modelClass) {
        ModelBuilder<T> builder = (ModelBuilder<T>) builders.get(modelClass);
        if (builder == null) {
            throw new IllegalArgumentException(
                    "No builder registered for: " + modelClass.getSimpleName()
            );
        }
        return builder;
    }

    /**
     * Quick access to build valid entity.
     */
    public <T> T valid(Class<T> modelClass) {
        return getBuilderFor(modelClass).build();
    }

    /**
     * Quick access to build with customization.
     */
    public <T> T validWith(Class<T> modelClass, Consumer<T> customizer) {
        return getBuilderFor(modelClass).build(customizer);
    }
}