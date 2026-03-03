package com.json.Users;

/**
 * UserEndpoint – a single, final repository of every URL path, header name,
 * schema path and numeric constant used by the Users resource tests.
 *
 * <p>
 * <b>SOLID – Single Responsibility:</b> only holds constant values specific
 * to the /users resource; no logic lives here.
 *
 * <p>
 * This class is intentionally {@code final} and has a private constructor
 * to prevent instantiation (it is a constants-utility class, not a bean).
 */
public final class UserEndpoint {

    // ─────────────────────────────────────────────────────────────────────────
    // Base URL
    // ─────────────────────────────────────────────────────────────────────────

    /** Root URL for the JSONPlaceholder public API – no trailing slash. */
    public static final String BASE_URI = "https://jsonplaceholder.typicode.com";

    // ─────────────────────────────────────────────────────────────────────────
    // Users Endpoint Paths
    // ─────────────────────────────────────────────────────────────────────────

    /** Collection endpoint: GET all users. */
    public static final String USERS = "/users";

    /** Single-resource endpoint: GET a user by id. */
    public static final String USER_BY_ID = "/users/{id}";

    // ─────────────────────────────────────────────────────────────────────────
    // SLA / Timing
    // ─────────────────────────────────────────────────────────────────────────

    /** Maximum acceptable response time in milliseconds (3-second SLA). */
    public static final long MAX_RESPONSE_TIME_MS = 3_000L;

    // ─────────────────────────────────────────────────────────────────────────
    // Numeric Test Fixtures
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * JSONPlaceholder seeds exactly 10 users; the collection must contain at least
     * this many.
     */
    public static final int EXPECTED_MIN_USER_COUNT = 10;

    /** A valid user id (1–10) used in GET /users/{id} happy-path tests. */
    public static final int VALID_USER_ID = 1;

    /** A user id that is guaranteed not to exist – triggers a 404 response. */
    public static final int INVALID_USER_ID = 9999;

    // ─────────────────────────────────────────────────────────────────────────
    // HTTP Header Names
    // ─────────────────────────────────────────────────────────────────────────

    /** The standard HTTP header used to describe the body format. */
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    // ─────────────────────────────────────────────────────────────────────────
    // JSON Schema File Paths
    // ─────────────────────────────────────────────────────────────────────────

    /** Classpath-relative path to the single-user JSON Schema draft-07 file. */
    public static final String SCHEMA_SINGLE_USER = "schemas/user-schema.json";

    // ─────────────────────────────────────────────────────────────────────────

    /** Private constructor – prevents instantiation of this utility class. */
    private UserEndpoint() {
        throw new UnsupportedOperationException("UserEndpoint is a constants class and cannot be instantiated.");
    }
}
