package com.json.Todos;

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
 * TodoPayload – POJO model for the /todos resource and its JSON Schema helpers.
 *
 * <p>
 * JSON shape:
 * 
 * <pre>
 * {
 *   "userId":    1,
 *   "id":        1,
 *   "title":     "delectus aut autem",
 *   "completed": false
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TodoPayload {

    /** The id of the user this todo belongs to. */
    @JsonProperty("userId")
    private Integer userId;

    /** Server-assigned unique identifier of this todo. */
    @JsonProperty("id")
    private Integer id;

    /** Title / description of the todo item. */
    @JsonProperty("title")
    private String title;

    /** Whether the todo has been completed. */
    @JsonProperty("completed")
    private Boolean completed;

    // ─────────────────────────────────────────────────────────────────────────
    // JSON Schema helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Returns a matcher validating against the single-todo JSON Schema. */
    public static Matcher<?> todoSchema() {
        return loadSchema(TodoEndpoint.SCHEMA_SINGLE_TODO);
    }

    private static Matcher<?> loadSchema(String classpathPath) {
        InputStream stream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(classpathPath);
        if (stream == null) {
            throw new IllegalArgumentException("JSON Schema not found on classpath: " + classpathPath);
        }
        return JsonSchemaValidator.matchesJsonSchemaInClasspath(classpathPath);
    }
}
