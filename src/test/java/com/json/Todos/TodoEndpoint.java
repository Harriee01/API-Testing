package com.json.Todos;

/**
 * TodoEndpoint – constants for the /todos resource.
 * JSONPlaceholder contains 200 todos (20 per user × 10 users).
 */
public final class TodoEndpoint {

    public static final String BASE_URI = "https://jsonplaceholder.typicode.com";

    /** GET all / POST new todo. */
    public static final String TODOS = "/todos";

    /** GET | PUT | PATCH | DELETE a single todo. */
    public static final String TODO_BY_ID = "/todos/{id}";

    /** Nested endpoint – GET all todos that belong to a specific user. */
    public static final String USER_TODOS = "/users/{userId}/todos";

    public static final long MAX_RESPONSE_TIME_MS = 3_000L;
    public static final int EXPECTED_MIN_TODO_COUNT = 200;
    public static final int VALID_TODO_ID = 1;
    public static final int INVALID_TODO_ID = 9999;
    public static final int VALID_USER_ID = 1;
    public static final int EXPECTED_TODOS_PER_USER = 20;

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String SCHEMA_SINGLE_TODO = "schemas/todo-schema.json";

    private TodoEndpoint() {
        throw new UnsupportedOperationException("TodoEndpoint is a constants class and cannot be instantiated.");
    }
}
