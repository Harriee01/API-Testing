package com.json.Albums;

import net.datafaker.Faker;

/**
 * AlbumDataProvider – factory for realistic, randomised test data
 * for the Albums resource.
 */
public final class AlbumDataProvider {

    private static final Faker FAKER = new Faker();

    private AlbumDataProvider() {
        throw new UnsupportedOperationException("AlbumDataProvider is a utility class and cannot be instantiated.");
    }

    /** Generates a realistic album title using a book-title pattern. */
    public static String randomAlbumTitle() {
        return FAKER.book().title();
    }

    /**
     * Returns a random userId in range [1, 10] (all users that exist in seed data).
     */
    public static int randomUserId() {
        return FAKER.number().numberBetween(1, 10);
    }
}
