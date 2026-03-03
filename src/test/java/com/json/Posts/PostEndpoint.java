package com.json.Posts;

/**
 * PostEndpoint – a single, final repository of every URL path, header name,
 * schema path and numeric constant used by the Posts resource tests.
 *
 * <p>
 * <b>SOLID – Single Responsibility:</b> only holds constant values specific
 * to the /posts resource; no logic lives here.
 *
 * <p>
 * This class is intentionally {@code final} and has a private constructor
 * to prevent instantiation (it is a constants-utility class, not a bean).
 */
public final class PostEndpoint {

    // ─────────────────────────────────────────────────────────────────────────
    // Base URL
    // ─────────────────────────────────────────────────────────────────────────

    /** Root URL for the JSONPlaceholder public API – no trailing slash. */
    public static final String BASE_URI = "https://jsonplaceholder.typicode.com";

    // ─────────────────────────────────────────────────────────────────────────
    // Posts Endpoint Paths
    // ─────────────────────────────────────────────────────────────────────────

    /** Collection endpoint: GET all posts / POST a new post. */
    public static final String POSTS = "/posts";

    /** Single-resource endpoint: GET | PUT | PATCH | DELETE a post by id. */
    public static final String POST_BY_ID = "/posts/{id}";

    // ─────────────────────────────────────────────────────────────────────────
    // SLA / Timing
    // ─────────────────────────────────────────────────────────────────────────

    /** Maximum acceptable response time in milliseconds (3-second SLA). */
    public static final long MAX_RESPONSE_TIME_MS = 3_000L;

    // ─────────────────────────────────────────────────────────────────────────
    // Numeric Test Fixtures
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * JSONPlaceholder seeds exactly 100 posts; the collection must contain at least
     * this many.
     */
    public static final int EXPECTED_MIN_POST_COUNT = 100;

    /**
     * The id returned by JSONPlaceholder when a POST /posts is performed – always
     * echoes id=101.
     */
    public static final int EXPECTED_CREATED_POST_ID = 101;

    /** A valid post id that exists in the seed data (1–100). */
    public static final int VALID_POST_ID = 1;

    /** A post id that is guaranteed NOT to exist – triggers a 404 response. */
    public static final int INVALID_POST_ID = 9999;

    // ─────────────────────────────────────────────────────────────────────────
    // HTTP Header Names
    // ─────────────────────────────────────────────────────────────────────────

    /** The standard HTTP header used to describe the body format. */
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    // ─────────────────────────────────────────────────────────────────────────
    // JSON Schema File Paths
    // ─────────────────────────────────────────────────────────────────────────

    /** Classpath-relative path to the single-post JSON Schema draft-07 file. */
    public static final String SCHEMA_SINGLE_POST = "schemas/post-schema.json";

    /** Classpath-relative path to the post-array JSON Schema draft-07 file. */
    public static final String SCHEMA_POST_ARRAY = "schemas/post-array-schema.json";

    // ─────────────────────────────────────────────────────────────────────────

    /** Private constructor – prevents instantiation of this utility class. */
    private PostEndpoint() {
        throw new UnsupportedOperationException("PostEndpoint is a constants class and cannot be instantiated.");
    }
}
