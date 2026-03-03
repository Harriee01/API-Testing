package com.json.Photos;

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
 * PhotoPayload – POJO model for the /photos resource and its JSON Schema
 * helpers.
 *
 * <p>
 * JSON shape:
 * 
 * <pre>
 * {
 *   "albumId":      1,
 *   "id":           1,
 *   "title":        "accusamus beatae ad facilis...",
 *   "url":          "https://via.placeholder.com/600/92c952",
 *   "thumbnailUrl": "https://via.placeholder.com/150/92c952"
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PhotoPayload {

    /** The id of the album this photo belongs to. */
    @JsonProperty("albumId")
    private Integer albumId;

    /** Server-assigned unique identifier of this photo. */
    @JsonProperty("id")
    private Integer id;

    /** Title / description of the photo. */
    @JsonProperty("title")
    private String title;

    /** Full-size image URL. */
    @JsonProperty("url")
    private String url;

    /** Thumbnail image URL. */
    @JsonProperty("thumbnailUrl")
    private String thumbnailUrl;

    // ─────────────────────────────────────────────────────────────────────────
    // JSON Schema helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Returns a matcher validating against the single-photo JSON Schema. */
    public static Matcher<?> photoSchema() {
        return loadSchema(PhotoEndpoint.SCHEMA_SINGLE_PHOTO);
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
