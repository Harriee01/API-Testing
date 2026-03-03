package com.json.Photos;

/**
 * PhotoEndpoint – constants for the /photos resource.
 * JSONPlaceholder contains 5000 photos (50 per album × 100 albums).
 * Note: INVALID_PHOTO_ID uses 99999 since valid IDs run up to 5000.
 */
public final class PhotoEndpoint {

    public static final String BASE_URI = "https://jsonplaceholder.typicode.com";

    /** GET all / POST new photo. */
    public static final String PHOTOS = "/photos";

    /** GET | PUT | PATCH | DELETE a single photo. */
    public static final String PHOTO_BY_ID = "/photos/{id}";

    /** Nested endpoint – GET all photos that belong to a specific album. */
    public static final String ALBUM_PHOTOS = "/albums/{albumId}/photos";

    public static final long MAX_RESPONSE_TIME_MS = 3_000L;
    public static final int EXPECTED_MIN_PHOTO_COUNT = 5000;
    public static final int VALID_PHOTO_ID = 1;
    public static final int INVALID_PHOTO_ID = 99999; // beyond 5000 ceiling
    public static final int VALID_ALBUM_ID = 1;
    public static final int EXPECTED_PHOTOS_PER_ALBUM = 50;

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String SCHEMA_SINGLE_PHOTO = "schemas/photo-schema.json";

    private PhotoEndpoint() {
        throw new UnsupportedOperationException("PhotoEndpoint is a constants class and cannot be instantiated.");
    }
}
