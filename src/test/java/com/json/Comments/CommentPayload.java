package com.json.Comments;

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
 * CommentPayload – POJO model for the /comments resource and its JSON Schema
 * helpers.
 *
 * <p>
 * JSON shape:
 * 
 * <pre>
 * {
 *   "postId": 1,
 *   "id":     1,
 *   "name":   "id labore ex et quam laborum",
 *   "email":  "Eliseo@gardner.biz",
 *   "body":   "laudantium enim quasi..."
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommentPayload {

    /** The id of the post this comment belongs to. */
    @JsonProperty("postId")
    private Integer postId;

    /** Server-assigned unique identifier of this comment. */
    @JsonProperty("id")
    private Integer id;

    /** Display name / subject heading of the comment. */
    @JsonProperty("name")
    private String name;

    /** Email of the comment author. */
    @JsonProperty("email")
    private String email;

    /** Full body text of the comment. */
    @JsonProperty("body")
    private String body;

    // ─────────────────────────────────────────────────────────────────────────
    // JSON Schema helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Returns a matcher validating against the single-comment JSON Schema. */
    public static Matcher<?> commentSchema() {
        return loadSchema(CommentEndpoint.SCHEMA_SINGLE_COMMENT);
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
