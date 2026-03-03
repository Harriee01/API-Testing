package com.json.Comments;

/**
 * CommentEndpoint – constants for the /comments resource.
 * JSONPlaceholder contains 500 comments (5 per post × 100 posts).
 */
public final class CommentEndpoint {

    public static final String BASE_URI = "https://jsonplaceholder.typicode.com";

    /** GET all / POST new comment. */
    public static final String COMMENTS = "/comments";

    /** GET | PUT | PATCH | DELETE a single comment. */
    public static final String COMMENT_BY_ID = "/comments/{id}";

    /** Nested endpoint – GET all comments that belong to a specific post. */
    public static final String POST_COMMENTS = "/posts/{postId}/comments";

    public static final long MAX_RESPONSE_TIME_MS = 3_000L;
    public static final int EXPECTED_MIN_COMMENT_COUNT = 500;
    public static final int VALID_COMMENT_ID = 1;
    public static final int INVALID_COMMENT_ID = 9999;
    public static final int VALID_POST_ID = 1;
    public static final int EXPECTED_COMMENTS_PER_POST = 5;

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String SCHEMA_SINGLE_COMMENT = "schemas/comment-schema.json";

    private CommentEndpoint() {
        throw new UnsupportedOperationException("CommentEndpoint is a constants class and cannot be instantiated.");
    }
}
