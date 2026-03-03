package com.json.Users;

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
 * UserPayload – combines the User POJO model and its JSON Schema validation
 * helpers.
 *
 * <p>
 * Maps to the JSON returned by {@code GET /users} and {@code GET /users/{id}}.
 * Only the fields most relevant to test assertions are captured here; all other
 * API fields are silently ignored via
 * {@code @JsonIgnoreProperties(ignoreUnknown = true)}.
 *
 * <p>
 * Sample JSON (abbreviated):
 * 
 * <pre>
 * {
 *   "id":       1,
 *   "name":     "Leanne Graham",
 *   "username": "Bret",
 *   "email":    "Sincere@april.biz",
 *   "phone":    "1-770-736-8031 x56442",
 *   "website":  "hildegard.org"
 * }
 * </pre>
 *
 * <p>
 * <b>SOLID – Open/Closed:</b> The model is open for extension but closed for
 * modification: tests asserting existing fields will never break when the API
 * adds new ones.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserPayload {

    /** Server-assigned unique user identifier. */
    @JsonProperty("id")
    private Integer id;

    /** Full display name of the user (e.g. "Leanne Graham"). */
    @JsonProperty("name")
    private String name;

    /** Short, unique username / handle (e.g. "Bret"). */
    @JsonProperty("username")
    private String username;

    /** User's email address – validated for presence in schema tests. */
    @JsonProperty("email")
    private String email;

    /** Contact phone number string (may include extensions). */
    @JsonProperty("phone")
    private String phone;

    /** User's personal website / URL. */
    @JsonProperty("website")
    private String website;

    // ─────────────────────────────────────────────────────────────────────────
    // JSON Schema validation helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns a Hamcrest matcher that validates a response body against the
     * single-user JSON Schema.
     *
     * <p>
     * Usage in a test:
     * 
     * <pre>
     * response.then().body(UserPayload.userSchema());
     * </pre>
     *
     * @return Hamcrest matcher for a single user object
     */
    public static Matcher<?> userSchema() {
        return loadSchema(UserEndpoint.SCHEMA_SINGLE_USER);
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
