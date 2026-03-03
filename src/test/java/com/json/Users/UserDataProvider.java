package com.json.Users;

import net.datafaker.Faker;

/**
 * UserDataProvider – a factory for realistic, randomised test data for the
 * Users resource.
 *
 * <p>
 * Uses <a href="https://www.datafaker.net/">DataFaker</a> to generate
 * locale-aware fake data so tests never rely on hard-coded strings.
 *
 * <p>
 * <b>SOLID – Single Responsibility:</b> this class is solely responsible for
 * producing randomised input values for User tests. No HTTP logic or assertion
 * code belongs here.
 */
public final class UserDataProvider {

    /** Shared DataFaker instance – thread-safe after construction. */
    private static final Faker FAKER = new Faker();

    /** Private constructor – utility class, never instantiated. */
    private UserDataProvider() {
        throw new UnsupportedOperationException("UserDataProvider is a utility class and cannot be instantiated.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // User data factory methods
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Generates a random full display name (e.g. "Alicia Hartmann").
     *
     * @return a non-null, non-empty random full name string
     */
    public static String randomFullName() {
        return FAKER.name().fullName();
    }

    /**
     * Generates a syntactically valid random email address.
     *
     * @return a random email string containing '@'
     */
    public static String randomEmail() {
        return FAKER.internet().emailAddress();
    }

    /**
     * Generates a random username handle.
     *
     * @return a short random username string
     */
    public static String randomUsername() {
        return FAKER.internet().username();
    }
}
