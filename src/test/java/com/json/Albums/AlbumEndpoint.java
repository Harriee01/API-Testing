package com.json.Albums;

/**
 * AlbumEndpoint – constants for the /albums resource.
 * JSONPlaceholder contains 100 albums (10 per user × 10 users).
 */
public final class AlbumEndpoint {

    public static final String BASE_URI = "https://jsonplaceholder.typicode.com";

    /** GET all / POST new album. */
    public static final String ALBUMS = "/albums";

    /** GET | PUT | PATCH | DELETE a single album. */
    public static final String ALBUM_BY_ID = "/albums/{id}";

    /** Nested endpoint – GET all albums that belong to a specific user. */
    public static final String USER_ALBUMS = "/users/{userId}/albums";

    public static final long MAX_RESPONSE_TIME_MS = 3_000L;
    public static final int EXPECTED_MIN_ALBUM_COUNT = 100;
    public static final int VALID_ALBUM_ID = 1;
    public static final int INVALID_ALBUM_ID = 9999;
    public static final int VALID_USER_ID = 1;
    public static final int EXPECTED_ALBUMS_PER_USER = 10;

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String SCHEMA_SINGLE_ALBUM = "schemas/album-schema.json";

    private AlbumEndpoint() {
        throw new UnsupportedOperationException("AlbumEndpoint is a constants class and cannot be instantiated.");
    }
}
