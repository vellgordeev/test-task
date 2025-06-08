package ru.gordeev.core.data;

import com.github.javafaker.Faker;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Generic builder for test data.
 * Provides fluent API for creating test entities.
 */
public class ModelBuilder<T> {

    private static final Faker FAKER = new Faker();
    private final Supplier<T> defaultSupplier;

    private ModelBuilder(Supplier<T> defaultSupplier) {
        this.defaultSupplier = defaultSupplier;
    }

    /**
     * Creates builder with default supplier.
     */
    public static <T> ModelBuilder<T> of(Supplier<T> defaultSupplier) {
        return new ModelBuilder<>(defaultSupplier);
    }

    /**
     * Builds entity with default values.
     */
    public T build() {
        return defaultSupplier.get();
    }

    /**
     * Builds entity with custom modifications.
     */
    public T build(Consumer<T> customizer) {
        T entity = defaultSupplier.get();
        customizer.accept(entity);
        return entity;
    }

    /**
     * Builds multiple entities.
     */
    public List<T> buildList(int count) {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(build());
        }
        return list;
    }

    // Utility methods for common data generation
    public static String uniqueText(String prefix) {
        return String.format("%s_%s_%d",
                prefix,
                UUID.randomUUID().toString().substring(0, 8),
                System.currentTimeMillis()
        );
    }

    public static String randomText() {
        return FAKER.lorem().sentence();
    }

    public static boolean randomBoolean() {
        return FAKER.bool().bool();
    }

    public static long randomLong(long min, long max) {
        return FAKER.number().numberBetween(min, max);
    }

    public static String randomEmail() {
        return FAKER.internet().emailAddress();
    }

    public static String randomPhone() {
        return FAKER.phoneNumber().phoneNumber();
    }
}