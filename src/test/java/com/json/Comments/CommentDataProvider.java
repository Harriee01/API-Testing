package com.json.Comments;

import net.datafaker.Faker;

/**
 * CommentDataProvider – factory for realistic, randomised test data
 * for the Comments resource.
 */
public final class CommentDataProvider {

    private static final Faker FAKER = new Faker();

    private CommentDataProvider() {
        throw new UnsupportedOperationException("CommentDataProvider is a utility class and cannot be instantiated.");
    }

    /** Generates a plausible comment heading / name. */
    public static String randomCommentName() {
        return FAKER.book().title();
    }

    /** Generates a multi-sentence comment body. */
    public static String randomCommentBody() {
        return FAKER.lorem().paragraph(2);
    }

    /** Generates a syntactically valid random email address. */
    public static String randomEmail() {
        return FAKER.internet().emailAddress();
    }

    /**
     * Returns a random postId in range [1, 100] (all posts that exist in seed
     * data).
     */
    public static int randomPostId() {
        return FAKER.number().numberBetween(1, 100);
    }
}
