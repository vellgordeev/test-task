package ru.gordeev.core.api;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Central registry for all API services.
 * Manages service instances and provides type-safe access.
 */
@Slf4j
public class ApiProvider {

    private final Map<Class<?>, Object> services = new HashMap<>();


    /**
     * Registers a service instance.
     */
    public <T> void register(Class<T> serviceClass, T serviceInstance) {
        services.put(serviceClass, serviceInstance);
    }

    /**
     * Gets a registered service by its class.
     *
     * @throws IllegalArgumentException if service not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> serviceClass) {
        T service = (T) services.get(serviceClass);
        if (service == null) {
            throw new IllegalArgumentException(
                    "Service not registered: " + serviceClass.getSimpleName() +
                            ". Available services: " + services.keySet()
            );
        }
        return service;
    }
}