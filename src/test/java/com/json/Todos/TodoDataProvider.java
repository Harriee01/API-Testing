package com.json.Todos;

import net.datafaker.Faker;

/**
 * TodoDataProvider – factory for realistic, randomised test data
 * for the Todos resource.
 */
public final class TodoDataProvider {

    private static final Faker FAKER = new Faker();

    private TodoDataProvider() {
        throw new UnsupportedOperationException("TodoDataProvider is a utility class and cannot be instantiated.");
    }

    /** Generates a realistic todo title (short sentence). */
    public static String randomTodoTitle() {
        return FAKER.lorem().sentence();
    }

    /** Returns a random boolean completion status. */
    public static boolean randomCompleted() {
        return FAKER.bool().bool();
    }

    /**
     * Returns a random userId in range [1, 10] (all users that exist in seed data).
     */
    public static int randomUserId() {
        return FAKER.number().numberBetween(1, 10);
    }
}
