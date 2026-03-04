package com.json.Posts;

import com.json.base.BaseTest;
import com.json.Constants.StatusCodes;
import io.qameta.allure.*;
import io.qameta.allure.Step;
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
 * PostTest – integration tests for the {@code /posts} resource of the
 * JSONPlaceholder public API.
 *
 * <p>
 * <b>Coverage summary:</b>
 * <ol>
 * <li>GET /posts – collection: 200, array size, schema, Content-Type</li>
 * <li>GET /posts/{id} – single item: 200, field values, schema</li>
 * <li>GET /posts/{id} – 404 for non-existent id (negative)</li>
 * <li>POST /posts – 201, echoed fields, server-assigned id=101</li>
 * <li>POST /posts – empty title field (negative boundary)</li>
 * <li>PUT /posts/{id} – 200, updated fields reflected in response</li>
 * <li>PATCH /posts/{id} – 200, partial update reflected</li>
 * <li>DELETE /posts/{id} – 200, empty response body</li>
 * <li>GET /posts?userId= – filter by query param, checks result count</li>
 * </ol>
 *
 * <p>
 * <b>SOLID compliance:</b>
 * <ul>
 * <li><b>S</b> – HTTP step helpers and assertions are co-located in one test
 * class
 * following the resource-centric package structure</li>
 * <li><b>D</b> – Depends on {@link PostPayload} and {@link PostEndpoint}
 * abstractions rather than duplicating magic strings</li>
 * </ul>
 */
@Epic("JSONPlaceholder API")
@Feature("Posts Resource")
@DisplayName("POST /posts & /posts/{id} Tests")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class PostTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(PostTest.class);

    // ─────────────────────────────────────────────────────────────────────────
    // HTTP Step Helpers – all /posts HTTP operations live here
    // ─────────────────────────────────────────────────────────────────────────

    @Step("GET all posts")
    private ValidatableResponse getAllPosts() {
        log.info("Step: GET {}", PostEndpoint.POSTS);
        return given().spec(requestSpec)
                .when().get(PostEndpoint.POSTS)
                .then().spec(buildResponseSpec());
    }

    @Step("GET post by id={postId}")
    private ValidatableResponse getPostById(int postId) {
        log.info("Step: GET /posts/{}", postId);
        return given().spec(requestSpec)
                .pathParam("id", postId)
                .when().get(PostEndpoint.POST_BY_ID)
                .then().spec(buildResponseSpec());
    }

    @Step("POST new post with title='{post.title}'")
    private ValidatableResponse createPost(PostPayload post) {
        log.info("Step: POST /posts – title: '{}'", post.getTitle());
        return given().spec(requestSpec)
                .body(post)
                .when().post(PostEndpoint.POSTS)
                .then().spec(buildResponseSpec());
    }

    @Step("PUT post id={postId} with updated data")
    private ValidatableResponse updatePost(int postId, PostPayload updatedPost) {
        log.info("Step: PUT /posts/{} – new title: '{}'", postId, updatedPost.getTitle());
        return given().spec(requestSpec)
                .pathParam("id", postId)
                .body(updatedPost)
                .when().put(PostEndpoint.POST_BY_ID)
                .then().spec(buildResponseSpec());
    }

    @Step("PATCH post id={postId} with partial data")
    private ValidatableResponse patchPost(int postId, PostPayload partialPost) {
        log.info("Step: PATCH /posts/{}", postId);
        return given().spec(requestSpec)
                .pathParam("id", postId)
                .body(partialPost)
                .when().patch(PostEndpoint.POST_BY_ID)
                .then().spec(buildResponseSpec());
    }

    @Step("DELETE post id={postId}")
    private ValidatableResponse deletePost(int postId) {
        log.info("Step: DELETE /posts/{}", postId);
        return given().spec(requestSpec)
                .pathParam("id", postId)
                .when().delete(PostEndpoint.POST_BY_ID)
                .then().spec(buildResponseSpec());
    }

    @Step("GET posts filtered by userId={userId}")
    private ValidatableResponse getPostsByUserId(int userId) {
        log.info("Step: GET /posts?userId={}", userId);
        return given().spec(requestSpec)
                .queryParam("userId", userId)
                .when().get(PostEndpoint.POSTS)
                .then().spec(buildResponseSpec());
    }

    /**
     * Builds a minimal ResponseSpecification that enforces the shared SLA timing
     * check.
     */
    private ResponseSpecification buildResponseSpec() {
        return new ResponseSpecBuilder()
                .expectResponseTime(lessThan(PostEndpoint.MAX_RESPONSE_TIME_MS))
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1 – GET /posts (collection)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Get all posts")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("GET /posts → 200 with ≥100 items, correct Content-Type and valid schema")
    @TmsLink("TC-001")
    void getAllPosts_returns200WithCorrectMetadata() {
        getAllPosts()
                // ── Status ──────────────────────────────────────────────────
                .statusCode(StatusCodes.OK)

                // ── Headers ─────────────────────────────────────────────────
                .header(PostEndpoint.HEADER_CONTENT_TYPE, containsString("application/json"))

                // ── Body – size ──────────────────────────────────────────────
                .body("$", hasSize(greaterThanOrEqualTo(PostEndpoint.EXPECTED_MIN_POST_COUNT)))

                // ── Body – structure of first element ────────────────────────
                .body("[0].id", notNullValue())
                .body("[0].userId", notNullValue())
                .body("[0].title", not(emptyOrNullString()))
                .body("[0].body", not(emptyOrNullString()))

                // ── JSON Schema validation ───────────────────────────────────
                .body(PostPayload.postArraySchema());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2 – GET /posts/{id} (happy path)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Get single post")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("GET /posts/1 → 200 with correct id, userId, title and body fields")
    @TmsLink("TC-002")
    void getPostById_validId_returns200WithExpectedFields() {
        var response = getPostById(PostEndpoint.VALID_POST_ID)
                .statusCode(StatusCodes.OK)
                .header(PostEndpoint.HEADER_CONTENT_TYPE, containsString("application/json"))
                .body(PostPayload.postSchema())
                .body("id", equalTo(PostEndpoint.VALID_POST_ID))
                .body("userId", equalTo(1))
                .body("title", not(emptyOrNullString()))
                .body("body", not(emptyOrNullString()))
                .extract().response();

        PostPayload returnedPost = response.as(PostPayload.class);

        assertThat(returnedPost.getId())
                .as("Post id must match the path parameter")
                .isEqualTo(PostEndpoint.VALID_POST_ID);

        assertThat(returnedPost.getTitle())
                .as("Title must not be blank")
                .isNotBlank();

        assertThat(returnedPost.getBody())
                .as("Body must not be blank")
                .isNotBlank();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3 – GET /posts/{id} – 404 (negative)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Get single post – not found")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("GET /posts/9999 → 404 for non-existent resource (negative test)")
    @TmsLink("TC-003")
    void getPostById_invalidId_returns404() {
        getPostById(PostEndpoint.INVALID_POST_ID)
                .statusCode(StatusCodes.NOT_FOUND);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4 – POST /posts (create – happy path)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Create new post")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("POST /posts → 201, echoes sent fields, id=101 assigned by server")
    @TmsLink("TC-004")
    void createPost_validPayload_returns201WithEchoedData() {
        PostPayload newPost = PostPayload.builder()
                .userId(PostDataProvider.randomUserId())
                .title(PostDataProvider.randomPostTitle())
                .body(PostDataProvider.randomPostBody())
                .build();

        var response = createPost(newPost)
                .statusCode(StatusCodes.CREATED)
                .header(PostEndpoint.HEADER_CONTENT_TYPE, containsString("application/json"))
                .body("title", equalTo(newPost.getTitle()))
                .body("body", equalTo(newPost.getBody()))
                .body("userId", equalTo(newPost.getUserId()))
                .body("id", equalTo(PostEndpoint.EXPECTED_CREATED_POST_ID))
                .extract().response();

        PostPayload createdPost = response.as(PostPayload.class);

        assertThat(createdPost.getId())
                .as("New post must receive the server-assigned id=101")
                .isEqualTo(PostEndpoint.EXPECTED_CREATED_POST_ID);

        assertThat(createdPost.getTitle())
                .as("Returned title must match what was sent")
                .isEqualTo(newPost.getTitle());

        assertThat(createdPost.getUserId())
                .as("Returned userId must match what was sent")
                .isEqualTo(newPost.getUserId());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5 – POST /posts – empty title (negative boundary)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Create new post – invalid payload")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("POST /posts with empty title → API still returns 201 (JSONPlaceholder quirk, documented)")
    @TmsLink("TC-005")
    @Issue("JSONPlaceholder-1")
    void createPost_emptyTitle_isDocumentedBehaviour() {
        PostPayload invalidPost = PostPayload.builder()
                .userId(1)
                .title("") // intentionally empty – real API would reject this
                .body("some body")
                .build();

        createPost(invalidPost)
                .statusCode(StatusCodes.CREATED)
                .body("title", equalTo(""));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6 – PUT /posts/{id} (full update)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Update post")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("PUT /posts/1 → 200, updated title and body reflected in response")
    @TmsLink("TC-006")
    void updatePost_validPayload_returns200WithUpdatedFields() {
        PostPayload updatedPost = PostPayload.builder()
                .id(PostEndpoint.VALID_POST_ID)
                .userId(1)
                .title(PostDataProvider.randomPostTitle())
                .body(PostDataProvider.randomPostBody())
                .build();

        var response = updatePost(PostEndpoint.VALID_POST_ID, updatedPost)
                .statusCode(StatusCodes.OK)
                .header(PostEndpoint.HEADER_CONTENT_TYPE, containsString("application/json"))
                .body("id", equalTo(PostEndpoint.VALID_POST_ID))
                .body("title", equalTo(updatedPost.getTitle()))
                .body("body", equalTo(updatedPost.getBody()))
                .extract().response();

        PostPayload returnedPost = response.as(PostPayload.class);

        assertThat(returnedPost.getTitle())
                .as("PUT response must reflect the updated title")
                .isEqualTo(updatedPost.getTitle());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7 – PATCH /posts/{id} (partial update)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Partially update post")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("PATCH /posts/1 → 200, only the patched title field changes")
    @TmsLink("TC-007")
    void patchPost_titleOnly_returns200WithPatchedTitle() {
        String patchedTitle = "Patched: " + PostDataProvider.randomPostTitle();
        PostPayload partialPost = PostPayload.builder()
                .title(patchedTitle)
                .build();

        patchPost(PostEndpoint.VALID_POST_ID, partialPost)
                .statusCode(StatusCodes.OK)
                .header(PostEndpoint.HEADER_CONTENT_TYPE, containsString("application/json"))
                .body("title", equalTo(patchedTitle));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8 – DELETE /posts/{id}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Delete post")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("DELETE /posts/1 → 200, response body is empty object {}")
    @TmsLink("TC-008")
    void deletePost_validId_returns200() {
        deletePost(PostEndpoint.VALID_POST_ID)
                .statusCode(StatusCodes.OK)
                .body("$", anEmptyMap());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 9 – GET /posts?userId=1 (query parameter filtering)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Filter posts by userId")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("GET /posts?userId=1 → 200, all returned posts belong to userId=1")
    @TmsLink("TC-009")
    void getPostsFilteredByUserId_returnsOnlyMatchingPosts() {
        final int filterUserId = 1;

        getPostsByUserId(filterUserId)
                .statusCode(StatusCodes.OK)
                .body("userId", everyItem(equalTo(filterUserId)))
                .body("$", hasSize(10));
    }
}
