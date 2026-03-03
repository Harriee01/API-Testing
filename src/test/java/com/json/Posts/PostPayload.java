package com.json.Posts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.restassured.module.jsv.JsonSchemaValidator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hamcrest.Matcher;

import java.io.InputStream;

/**
 * PostPayload – combines the Post POJO model and its JSON Schema validation
 * helpers.
 *
 * <p>
 * Maps directly to the JSON shape returned by {@code GET /posts} and
 * {@code GET /posts/{id}}:
 *
 * <pre>
 * {
 *   "userId": 1,
 *   "id":     1,
 *   "title":  "sunt aut ...",
 *   "body":   "quia et ..."
 * }
 * </pre>
 *
 * <p>
 * <b>Lombok annotations used:</b>
 * <ul>
 * <li>{@code @Data} – generates getters, setters, equals, hashCode,
 * toString</li>
 * <li>{@code @Builder} – provides a fluent builder for constructing instances
 * in tests</li>
 * <li>{@code @NoArgsConstructor} – required by Jackson for deserialization</li>
 * <li>{@code @AllArgsConstructor} – required by Lombok's @Builder when @NoArgs
 * is present</li>
 * </ul>
 *
 * <p>
 * {@code @JsonIgnoreProperties(ignoreUnknown = true)} makes the model resilient
 * to future fields added by the API (Open/Closed Principle).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostPayload {

    /** The id of the user who authored this post. */
    @JsonProperty("userId")
    private Integer userId;

    /**
     * The unique identifier of this post (server-assigned). For POST /posts
     * JSONPlaceholder always returns id=101.
     */
    @JsonProperty("id")
    private Integer id;

    /** The title of the post – a short descriptive string. */
    @JsonProperty("title")
    private String title;

    /** The full body (content) of the post. */
    @JsonProperty("body")
    private String body;

    // ─────────────────────────────────────────────────────────────────────────
    // JSON Schema validation helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns a Hamcrest matcher that validates a response body against the
     * single-post JSON Schema.
     *
     * <p>
     * Usage in a test:
     * 
     * <pre>
     * response.then().body(PostPayload.postSchema());
     * </pre>
     *
     * @return Hamcrest matcher for a single post object
     */
    public static Matcher<?> postSchema() {
        return loadSchema(PostEndpoint.SCHEMA_SINGLE_POST);
    }

    /**
     * Returns a matcher validating a response body against the post-array schema.
     *
     * @return Hamcrest matcher for an array of post objects
     */
    public static Matcher<?> postArraySchema() {
        return loadSchema(PostEndpoint.SCHEMA_POST_ARRAY);
    }

    /**
     * Resolves {@code classpathPath} via the thread-context classloader and
     * returns a Hamcrest matcher for REST Assured {@code .body(matcher)}
     * assertions.
     *
     * @param classpathPath path relative to the classpath root
     * @return a JsonSchemaValidator Hamcrest matcher
     * @throws IllegalArgumentException if the schema file cannot be found on the
     *                                  classpath
     */
    private static Matcher<?> loadSchema(String classpathPath) {
        InputStream stream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(classpathPath);

        if (stream == null) {
            throw new IllegalArgumentException(
                    "JSON Schema not found on classpath: " + classpathPath
                            + ". Ensure the file exists under src/main/resources/" + classpathPath);
        }
        return JsonSchemaValidator.matchesJsonSchemaInClasspath(classpathPath);
    }
}
