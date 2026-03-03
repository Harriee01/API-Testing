package com.json.Photos;

import net.datafaker.Faker;

/**
 * PhotoDataProvider – factory for realistic, randomised test data
 * for the Photos resource.
 */
public final class PhotoDataProvider {

    private static final Faker FAKER = new Faker();

    private PhotoDataProvider() {
        throw new UnsupportedOperationException("PhotoDataProvider is a utility class and cannot be instantiated.");
    }

    /** Generates a realistic photo title (short sentence). */
    public static String randomPhotoTitle() {
        return FAKER.lorem().sentence();
    }

    /** Generates a plausible placeholder photo URL. */
    public static String randomPhotoUrl() {
        String hex = FAKER.color().hex().replace("#", "");
        return "https://via.placeholder.com/600/" + hex;
    }

    /** Generates a plausible placeholder thumbnail URL. */
    public static String randomThumbnailUrl() {
        String hex = FAKER.color().hex().replace("#", "");
        return "https://via.placeholder.com/150/" + hex;
    }

    /**
     * Returns a random albumId in range [1, 100] (all albums that exist in seed
     * data).
     */
    public static int randomAlbumId() {
        return FAKER.number().numberBetween(1, 100);
    }
}
