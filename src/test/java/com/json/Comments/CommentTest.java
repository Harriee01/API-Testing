package com.json.Comments;

import com.json.Base.BaseTest;
import com.json.Constants.StatusCodes;
import io.qameta.allure.*;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * CommentTest – integration tests for the {@code /comments} resource.
 *
 * <p>
 * <b>Coverage:</b>
 * <ol>
 * <li>GET /comments → 200, ≥500 items, content-type, schema</li>
 * <li>GET /comments/{id} → 200, all fields present, POJO check</li>
 * <li>GET /comments/{id} → 404 for non-existent id (negative)</li>
 * <li>GET /posts/{postId}/comments → 200, 5 comments for post 1</li>
 * <li>POST /comments → 201, echoed fields</li>
 * <li>PUT /comments/{id} → 200, updated fields reflected</li>
 * <li>PATCH /comments/{id} → 200, partial update reflected</li>
 * <li>DELETE /comments/{id} → 200, empty response body</li>
 * <li>GET /comments → every comment email contains '@' (data-integrity)</li>
 * </ol>
 */
@Epic("JSONPlaceholder API")
@Feature("Comments Resource")
@DisplayName("/comments & /posts/{postId}/comments Tests")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class CommentTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(CommentTest.class);

    // ─────────────────────────────────────────────────────────────────────────
    // HTTP Step Helpers
    // ─────────────────────────────────────────────────────────────────────────

    @Step("GET all comments")
    private ValidatableResponse getAllComments() {
        log.info("Step: GET {}", CommentEndpoint.COMMENTS);
        return given().spec(requestSpec)
                .when().get(CommentEndpoint.COMMENTS)
                .then().spec(buildResponseSpec());
    }

    @Step("GET comment by id={commentId}")
    private ValidatableResponse getCommentById(int commentId) {
        log.info("Step: GET /comments/{}", commentId);
        return given().spec(requestSpec)
                .pathParam("id", commentId)
                .when().get(CommentEndpoint.COMMENT_BY_ID)
                .then().spec(buildResponseSpec());
    }

    @Step("GET comments for post id={postId}")
    private ValidatableResponse getCommentsByPostId(int postId) {
        log.info("Step: GET /posts/{}/comments", postId);
        return given().spec(requestSpec)
                .pathParam("postId", postId)
                .when().get(CommentEndpoint.POST_COMMENTS)
                .then().spec(buildResponseSpec());
    }

    @Step("POST new comment")
    private ValidatableResponse createComment(CommentPayload comment) {
        log.info("Step: POST /comments – name: '{}'", comment.getName());
        return given().spec(requestSpec)
                .body(comment)
                .when().post(CommentEndpoint.COMMENTS)
                .then().spec(buildResponseSpec());
    }

    @Step("PUT comment id={commentId} with updated data")
    private ValidatableResponse updateComment(int commentId, CommentPayload updated) {
        log.info("Step: PUT /comments/{}", commentId);
        return given().spec(requestSpec)
                .pathParam("id", commentId)
                .body(updated)
                .when().put(CommentEndpoint.COMMENT_BY_ID)
                .then().spec(buildResponseSpec());
    }

    @Step("PATCH comment id={commentId} with partial data")
    private ValidatableResponse patchComment(int commentId, CommentPayload partial) {
        log.info("Step: PATCH /comments/{}", commentId);
        return given().spec(requestSpec)
                .pathParam("id", commentId)
                .body(partial)
                .when().patch(CommentEndpoint.COMMENT_BY_ID)
                .then().spec(buildResponseSpec());
    }

    @Step("DELETE comment id={commentId}")
    private ValidatableResponse deleteComment(int commentId) {
        log.info("Step: DELETE /comments/{}", commentId);
        return given().spec(requestSpec)
                .pathParam("id", commentId)
                .when().delete(CommentEndpoint.COMMENT_BY_ID)
                .then().spec(buildResponseSpec());
    }

    private ResponseSpecification buildResponseSpec() {
        return new ResponseSpecBuilder()
                .expectResponseTime(lessThan(CommentEndpoint.MAX_RESPONSE_TIME_MS))
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1 – GET /comments (collection)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Get all comments")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("GET /comments → 200 with ≥500 items and correct Content-Type")
    @TmsLink("TC-014")
    void getAllComments_returns200WithAtLeast500Comments() {
        getAllComments()
                .statusCode(StatusCodes.OK)
                .header(CommentEndpoint.HEADER_CONTENT_TYPE, containsString("application/json"))
                .body("$", hasSize(greaterThanOrEqualTo(CommentEndpoint.EXPECTED_MIN_COMMENT_COUNT)))
                .body("[0].postId", notNullValue())
                .body("[0].id", notNullValue())
                .body("[0].name", not(emptyOrNullString()))
                .body("[0].email", not(emptyOrNullString()))
                .body("[0].body", not(emptyOrNullString()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2 – GET /comments/{id} (happy path)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Get single comment")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("GET /comments/1 → 200 with all mandatory fields and valid schema")
    @TmsLink("TC-015")
    void getCommentById_validId_returns200WithAllFields() {
        var response = getCommentById(CommentEndpoint.VALID_COMMENT_ID)
                .statusCode(StatusCodes.OK)
                .header(CommentEndpoint.HEADER_CONTENT_TYPE, containsString("application/json"))
                .body(CommentPayload.commentSchema())
                .body("id", equalTo(CommentEndpoint.VALID_COMMENT_ID))
                .body("postId", notNullValue())
                .body("name", not(emptyOrNullString()))
                .body("email", containsString("@"))
                .body("body", not(emptyOrNullString()))
                .extract().response();

        CommentPayload returned = response.as(CommentPayload.class);

        assertThat(returned.getId()).as("id must match path param").isEqualTo(CommentEndpoint.VALID_COMMENT_ID);
        assertThat(returned.getEmail()).as("email must contain @").contains("@");
        assertThat(returned.getBody()).as("body must not be blank").isNotBlank();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3 – GET /comments/{id} → 404 (negative)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Get single comment – not found")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("GET /comments/9999 → 404 for non-existent resource (negative test)")
    @TmsLink("TC-016")
    void getCommentById_invalidId_returns404() {
        getCommentById(CommentEndpoint.INVALID_COMMENT_ID)
                .statusCode(StatusCodes.NOT_FOUND);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4 – GET /posts/{postId}/comments (nested resource)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Get comments by post")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("GET /posts/1/comments → 200, exactly 5 comments all belonging to postId=1")
    @TmsLink("TC-017")
    void getCommentsByPostId_returns5CommentsForPost1() {
        getCommentsByPostId(CommentEndpoint.VALID_POST_ID)
                .statusCode(StatusCodes.OK)
                .body("$", hasSize(CommentEndpoint.EXPECTED_COMMENTS_PER_POST))
                .body("postId", everyItem(equalTo(CommentEndpoint.VALID_POST_ID)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5 – POST /comments (create)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Create new comment")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("POST /comments → 201, echoes sent fields, server assigns id=501")
    @TmsLink("TC-018")
    void createComment_validPayload_returns201WithEchoedData() {
        CommentPayload newComment = CommentPayload.builder()
                .postId(CommentDataProvider.randomPostId())
                .name(CommentDataProvider.randomCommentName())
                .email(CommentDataProvider.randomEmail())
                .body(CommentDataProvider.randomCommentBody())
                .build();

        var response = createComment(newComment)
                .statusCode(StatusCodes.CREATED)
                .header(CommentEndpoint.HEADER_CONTENT_TYPE, containsString("application/json"))
                .body("name", equalTo(newComment.getName()))
                .body("email", equalTo(newComment.getEmail()))
                .body("body", equalTo(newComment.getBody()))
                .body("postId", equalTo(newComment.getPostId()))
                .body("id", notNullValue())
                .extract().response();

        CommentPayload created = response.as(CommentPayload.class);
        assertThat(created.getId()).as("Server must assign an id").isNotNull();
        assertThat(created.getName()).as("Returned name must match sent name").isEqualTo(newComment.getName());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6 – PUT /comments/{id} (full update)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Update comment")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("PUT /comments/1 → 200, updated name and body reflected in response")
    @TmsLink("TC-019")
    void updateComment_validPayload_returns200WithUpdatedFields() {
        CommentPayload updated = CommentPayload.builder()
                .postId(1)
                .id(CommentEndpoint.VALID_COMMENT_ID)
                .name(CommentDataProvider.randomCommentName())
                .email(CommentDataProvider.randomEmail())
                .body(CommentDataProvider.randomCommentBody())
                .build();

        updateComment(CommentEndpoint.VALID_COMMENT_ID, updated)
                .statusCode(StatusCodes.OK)
                .header(CommentEndpoint.HEADER_CONTENT_TYPE, containsString("application/json"))
                .body("name", equalTo(updated.getName()))
                .body("body", equalTo(updated.getBody()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7 – PATCH /comments/{id} (partial update)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Partially update comment")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("PATCH /comments/1 → 200, only the patched body field changes")
    @TmsLink("TC-020")
    void patchComment_bodyOnly_returns200WithPatchedBody() {
        String patchedBody = "Patched: " + CommentDataProvider.randomCommentBody();
        CommentPayload partial = CommentPayload.builder().body(patchedBody).build();

        patchComment(CommentEndpoint.VALID_COMMENT_ID, partial)
                .statusCode(StatusCodes.OK)
                .body("body", equalTo(patchedBody));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8 – DELETE /comments/{id}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Delete comment")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("DELETE /comments/1 → 200, response body is empty object {}")
    @TmsLink("TC-021")
    void deleteComment_validId_returns200() {
        deleteComment(CommentEndpoint.VALID_COMMENT_ID)
                .statusCode(StatusCodes.OK)
                .body("$", anEmptyMap());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 9 – Data integrity: all emails contain '@'
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Validate comment data integrity")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("GET /comments → every comment email contains '@' (data-integrity check)")
    @TmsLink("TC-022")
    void getAllComments_everyEmailContainsAtSign() {
        getAllComments()
                .statusCode(StatusCodes.OK)
                .body("email", everyItem(containsString("@")));
    }
}
