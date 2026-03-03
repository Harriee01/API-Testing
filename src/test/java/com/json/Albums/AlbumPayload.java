package com.json.Albums;

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
 * AlbumPayload – POJO model for the /albums resource and its JSON Schema
 * helpers.
 *
 * <p>
 * JSON shape:
 * 
 * <pre>
 * {
 *   "userId": 1,
 *   "id":     1,
 *   "title":  "quidem molestiae enim"
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlbumPayload {

    /** The id of the user who owns this album. */
    @JsonProperty("userId")
    private Integer userId;

    /** Server-assigned unique identifier of this album. */
    @JsonProperty("id")
    private Integer id;

    /** Title / name of the album. */
    @JsonProperty("title")
    private String title;

    // ─────────────────────────────────────────────────────────────────────────
    // JSON Schema helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Returns a matcher validating against the single-album JSON Schema. */
    public static Matcher<?> albumSchema() {
        return loadSchema(AlbumEndpoint.SCHEMA_SINGLE_ALBUM);
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
