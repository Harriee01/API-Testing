package com.json.Posts;

import net.datafaker.Faker;

/**
 * PostDataProvider – a factory for realistic, randomised test payloads for the
 * Posts resource.
 *
 * <p>
 * Uses <a href="https://www.datafaker.net/">DataFaker</a> to generate
 * locale-aware fake data so tests never depend on hard-coded strings that
 * could accidentally match seed data or production records.
 *
 * <p>
 * <b>SOLID – Single Responsibility:</b> this class is solely responsible for
 * producing randomised input values for Post tests. No HTTP logic or assertion
 * code belongs here.
 *
 * <p>
 * All methods are {@code static} because this class carries no mutable state
 * beyond the shared {@link Faker} instance (which is thread-safe after
 * construction).
 */
public final class PostDataProvider {

    /**
     * Shared DataFaker instance – reusing one instance is more efficient than
     * creating a new {@link Faker} per call, and the class is thread-safe.
     */
    private static final Faker FAKER = new Faker();

    /** Private constructor – utility class, never instantiated. */
    private PostDataProvider() {
        throw new UnsupportedOperationException("PostDataProvider is a utility class and cannot be instantiated.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Post data factory methods
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Generates a random post title using a realistic book-title pattern.
     *
     * <p>
     * Example output: {@code "The Hollow Prince"}
     *
     * @return a non-null, non-empty random post title string
     */
    public static String randomPostTitle() {
        return FAKER.book().title();
    }

    /**
     * Generates a realistic multi-sentence post body using Lorem Ipsum paragraphs.
     *
     * <p>
     * Using 3 sentences keeps the payload reasonably sized while exercising
     * the body field validation path in tests.
     *
     * @return a non-null, non-empty random post body string
     */
    public static String randomPostBody() {
        return FAKER.lorem().paragraph(3);
    }

    /**
     * Returns a random user id in the range [1, 10] (matching JSONPlaceholder's
     * seeded user set), so that created posts reference a plausible author.
     *
     * @return integer in [1, 10]
     */
    public static int randomUserId() {
        return FAKER.number().numberBetween(1, 10);
    }
}
