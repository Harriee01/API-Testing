package com.json.Albums;

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
 * AlbumTest – integration tests for the {@code /albums} resource.
 *
 * <p>
 * <b>Coverage:</b>
 * <ol>
 * <li>GET /albums → 200, ≥100 items, content-type, schema</li>
 * <li>GET /albums/{id} → 200, all fields present, POJO check</li>
 * <li>GET /albums/{id} → 404 for non-existent id (negative)</li>
 * <li>GET /users/{userId}/albums → 200, 10 albums for user 1</li>
 * <li>POST /albums → 201, echoed fields</li>
 * <li>PUT /albums/{id} → 200, updated title reflected</li>
 * <li>PATCH /albums/{id} → 200, partial update reflected</li>
 * <li>DELETE /albums/{id} → 200, empty response body</li>
 * </ol>
 */
@Epic("JSONPlaceholder API")
@Feature("Albums Resource")
@DisplayName("/albums & /users/{userId}/albums Tests")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class AlbumTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(AlbumTest.class);

    // ─────────────────────────────────────────────────────────────────────────
    // HTTP Step Helpers
    // ─────────────────────────────────────────────────────────────────────────

    @Step("GET all albums")
    private ValidatableResponse getAllAlbums() {
        log.info("Step: GET {}", AlbumEndpoint.ALBUMS);
        return given().spec(requestSpec)
                .when().get(AlbumEndpoint.ALBUMS)
                .then().spec(buildResponseSpec());
    }

    @Step("GET album by id={albumId}")
    private ValidatableResponse getAlbumById(int albumId) {
        log.info("Step: GET /albums/{}", albumId);
        return given().spec(requestSpec)
                .pathParam("id", albumId)
                .when().get(AlbumEndpoint.ALBUM_BY_ID)
                .then().spec(buildResponseSpec());
    }

    @Step("GET albums for user id={userId}")
    private ValidatableResponse getAlbumsByUserId(int userId) {
        log.info("Step: GET /users/{}/albums", userId);
        return given().spec(requestSpec)
                .pathParam("userId", userId)
                .when().get(AlbumEndpoint.USER_ALBUMS)
                .then().spec(buildResponseSpec());
    }

    @Step("POST new album")
    private ValidatableResponse createAlbum(AlbumPayload album) {
        log.info("Step: POST /albums – title: '{}'", album.getTitle());
        return given().spec(requestSpec)
                .body(album)
                .when().post(AlbumEndpoint.ALBUMS)
                .then().spec(buildResponseSpec());
    }

    @Step("PUT album id={albumId} with updated data")
    private ValidatableResponse updateAlbum(int albumId, AlbumPayload updated) {
        log.info("Step: PUT /albums/{}", albumId);
        return given().spec(requestSpec)
                .pathParam("id", albumId)
                .body(updated)
                .when().put(AlbumEndpoint.ALBUM_BY_ID)
                .then().spec(buildResponseSpec());
    }

    @Step("PATCH album id={albumId} with partial data")
    private ValidatableResponse patchAlbum(int albumId, AlbumPayload partial) {
        log.info("Step: PATCH /albums/{}", albumId);
        return given().spec(requestSpec)
                .pathParam("id", albumId)
                .body(partial)
                .when().patch(AlbumEndpoint.ALBUM_BY_ID)
                .then().spec(buildResponseSpec());
    }

    @Step("DELETE album id={albumId}")
    private ValidatableResponse deleteAlbum(int albumId) {
        log.info("Step: DELETE /albums/{}", albumId);
        return given().spec(requestSpec)
                .pathParam("id", albumId)
                .when().delete(AlbumEndpoint.ALBUM_BY_ID)
                .then().spec(buildResponseSpec());
    }

    private ResponseSpecification buildResponseSpec() {
        return new ResponseSpecBuilder()
                .expectResponseTime(lessThan(AlbumEndpoint.MAX_RESPONSE_TIME_MS))
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1 – GET /albums (collection)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Get all albums")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("GET /albums → 200 with ≥100 items, correct Content-Type and valid schema")
    @TmsLink("TC-023")
    void getAllAlbums_returns200WithAtLeast100Albums() {
        getAllAlbums()
                .statusCode(StatusCodes.OK)
                .header(AlbumEndpoint.HEADER_CONTENT_TYPE, containsString("application/json"))
                .body("$", hasSize(greaterThanOrEqualTo(AlbumEndpoint.EXPECTED_MIN_ALBUM_COUNT)))
                .body("[0].id", notNullValue())
                .body("[0].userId", notNullValue())
                .body("[0].title", not(emptyOrNullString()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2 – GET /albums/{id} (happy path)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Get single album")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("GET /albums/1 → 200 with all mandatory fields and valid schema")
    @TmsLink("TC-024")
    void getAlbumById_validId_returns200WithAllFields() {
        var response = getAlbumById(AlbumEndpoint.VALID_ALBUM_ID)
                .statusCode(StatusCodes.OK)
                .header(AlbumEndpoint.HEADER_CONTENT_TYPE, containsString("application/json"))
                .body(AlbumPayload.albumSchema())
                .body("id", equalTo(AlbumEndpoint.VALID_ALBUM_ID))
                .body("userId", notNullValue())
                .body("title", not(emptyOrNullString()))
                .extract().response();

        AlbumPayload returned = response.as(AlbumPayload.class);
        assertThat(returned.getId()).as("id must match path param").isEqualTo(AlbumEndpoint.VALID_ALBUM_ID);
        assertThat(returned.getTitle()).as("title must not be blank").isNotBlank();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3 – GET /albums/{id} → 404 (negative)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Get single album – not found")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("GET /albums/9999 → 404 for non-existent resource (negative test)")
    @TmsLink("TC-025")
    void getAlbumById_invalidId_returns404() {
        getAlbumById(AlbumEndpoint.INVALID_ALBUM_ID)
                .statusCode(StatusCodes.NOT_FOUND);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4 – GET /users/{userId}/albums (nested resource)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Get albums by user")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("GET /users/1/albums → 200, exactly 10 albums all belonging to userId=1")
    @TmsLink("TC-026")
    void getAlbumsByUserId_returns10AlbumsForUser1() {
        getAlbumsByUserId(AlbumEndpoint.VALID_USER_ID)
                .statusCode(StatusCodes.OK)
                .body("$", hasSize(AlbumEndpoint.EXPECTED_ALBUMS_PER_USER))
                .body("userId", everyItem(equalTo(AlbumEndpoint.VALID_USER_ID)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5 – POST /albums (create)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Create new album")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("POST /albums → 201, echoes sent fields, server assigns new id")
    @TmsLink("TC-027")
    void createAlbum_validPayload_returns201WithEchoedData() {
        AlbumPayload newAlbum = AlbumPayload.builder()
                .userId(AlbumDataProvider.randomUserId())
                .title(AlbumDataProvider.randomAlbumTitle())
                .build();

        var response = createAlbum(newAlbum)
                .statusCode(StatusCodes.CREATED)
                .header(AlbumEndpoint.HEADER_CONTENT_TYPE, containsString("application/json"))
                .body("title", equalTo(newAlbum.getTitle()))
                .body("userId", equalTo(newAlbum.getUserId()))
                .body("id", notNullValue())
                .extract().response();

        AlbumPayload created = response.as(AlbumPayload.class);
        assertThat(created.getTitle()).as("Returned title must match sent title").isEqualTo(newAlbum.getTitle());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6 – PUT /albums/{id} (full update)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Update album")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("PUT /albums/1 → 200, updated title reflected in response")
    @TmsLink("TC-028")
    void updateAlbum_validPayload_returns200WithUpdatedTitle() {
        AlbumPayload updated = AlbumPayload.builder()
                .id(AlbumEndpoint.VALID_ALBUM_ID)
                .userId(1)
                .title(AlbumDataProvider.randomAlbumTitle())
                .build();

        updateAlbum(AlbumEndpoint.VALID_ALBUM_ID, updated)
                .statusCode(StatusCodes.OK)
                .body("title", equalTo(updated.getTitle()))
                .body("id", equalTo(AlbumEndpoint.VALID_ALBUM_ID));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7 – PATCH /albums/{id} (partial update)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Partially update album")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("PATCH /albums/1 → 200, only the patched title changes")
    @TmsLink("TC-029")
    void patchAlbum_titleOnly_returns200WithPatchedTitle() {
        String patchedTitle = "Patched: " + AlbumDataProvider.randomAlbumTitle();
        AlbumPayload partial = AlbumPayload.builder().title(patchedTitle).build();

        patchAlbum(AlbumEndpoint.VALID_ALBUM_ID, partial)
                .statusCode(StatusCodes.OK)
                .body("title", equalTo(patchedTitle));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8 – DELETE /albums/{id}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Delete album")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("DELETE /albums/1 → 200, response body is empty object {}")
    @TmsLink("TC-030")
    void deleteAlbum_validId_returns200() {
        deleteAlbum(AlbumEndpoint.VALID_ALBUM_ID)
                .statusCode(StatusCodes.OK)
                .body("$", anEmptyMap());
    }
}
