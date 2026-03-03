package com.json.Photos;

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
 * PhotoTest – integration tests for the {@code /photos} resource.
 *
 * <p>
 * <b>Coverage:</b>
 * <ol>
 * <li>GET /photos → 200, ≥5000 items, content-type</li>
 * <li>GET /photos/{id} → 200, all fields present, schema, POJO check</li>
 * <li>GET /photos/{id} → 404 for non-existent id (negative)</li>
 * <li>GET /albums/{albumId}/photos → 200, 50 photos for album 1</li>
 * <li>POST /photos → 201, echoed fields</li>
 * <li>PUT /photos/{id} → 200, updated title reflected</li>
 * <li>PATCH /photos/{id} → 200, partial update reflected</li>
 * <li>DELETE /photos/{id} → 200, empty response body</li>
 * </ol>
 */
@Epic("JSONPlaceholder API")
@Feature("Photos Resource")
@DisplayName("/photos & /albums/{albumId}/photos Tests")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class PhotoTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(PhotoTest.class);

    // ─────────────────────────────────────────────────────────────────────────
    // HTTP Step Helpers
    // ─────────────────────────────────────────────────────────────────────────

    @Step("GET all photos")
    private ValidatableResponse getAllPhotos() {
        log.info("Step: GET {}", PhotoEndpoint.PHOTOS);
        return given().spec(requestSpec)
                .when().get(PhotoEndpoint.PHOTOS)
                .then().spec(buildResponseSpec());
    }

    @Step("GET photo by id={photoId}")
    private ValidatableResponse getPhotoById(int photoId) {
        log.info("Step: GET /photos/{}", photoId);
        return given().spec(requestSpec)
                .pathParam("id", photoId)
                .when().get(PhotoEndpoint.PHOTO_BY_ID)
                .then().spec(buildResponseSpec());
    }

    @Step("GET photos for album id={albumId}")
    private ValidatableResponse getPhotosByAlbumId(int albumId) {
        log.info("Step: GET /albums/{}/photos", albumId);
        return given().spec(requestSpec)
                .pathParam("albumId", albumId)
                .when().get(PhotoEndpoint.ALBUM_PHOTOS)
                .then().spec(buildResponseSpec());
    }

    @Step("POST new photo")
    private ValidatableResponse createPhoto(PhotoPayload photo) {
        log.info("Step: POST /photos – title: '{}'", photo.getTitle());
        return given().spec(requestSpec)
                .body(photo)
                .when().post(PhotoEndpoint.PHOTOS)
                .then().spec(buildResponseSpec());
    }

    @Step("PUT photo id={photoId} with updated data")
    private ValidatableResponse updatePhoto(int photoId, PhotoPayload updated) {
        log.info("Step: PUT /photos/{}", photoId);
        return given().spec(requestSpec)
                .pathParam("id", photoId)
                .body(updated)
                .when().put(PhotoEndpoint.PHOTO_BY_ID)
                .then().spec(buildResponseSpec());
    }

    @Step("PATCH photo id={photoId} with partial data")
    private ValidatableResponse patchPhoto(int photoId, PhotoPayload partial) {
        log.info("Step: PATCH /photos/{}", photoId);
        return given().spec(requestSpec)
                .pathParam("id", photoId)
                .body(partial)
                .when().patch(PhotoEndpoint.PHOTO_BY_ID)
                .then().spec(buildResponseSpec());
    }

    @Step("DELETE photo id={photoId}")
    private ValidatableResponse deletePhoto(int photoId) {
        log.info("Step: DELETE /photos/{}", photoId);
        return given().spec(requestSpec)
                .pathParam("id", photoId)
                .when().delete(PhotoEndpoint.PHOTO_BY_ID)
                .then().spec(buildResponseSpec());
    }

    private ResponseSpecification buildResponseSpec() {
        return new ResponseSpecBuilder()
                .expectResponseTime(lessThan(PhotoEndpoint.MAX_RESPONSE_TIME_MS))
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1 – GET /photos (collection)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Get all photos")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("GET /photos → 200 with ≥5000 items and correct Content-Type")
    @TmsLink("TC-031")
    void getAllPhotos_returns200WithAtLeast5000Photos() {
        getAllPhotos()
                .statusCode(StatusCodes.OK)
                .header(PhotoEndpoint.HEADER_CONTENT_TYPE, containsString("application/json"))
                .body("$", hasSize(greaterThanOrEqualTo(PhotoEndpoint.EXPECTED_MIN_PHOTO_COUNT)))
                .body("[0].albumId", notNullValue())
                .body("[0].id", notNullValue())
                .body("[0].title", not(emptyOrNullString()))
                .body("[0].url", not(emptyOrNullString()))
                .body("[0].thumbnailUrl", not(emptyOrNullString()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2 – GET /photos/{id} (happy path)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Get single photo")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("GET /photos/1 → 200 with all mandatory fields and valid schema")
    @TmsLink("TC-032")
    void getPhotoById_validId_returns200WithAllFields() {
        var response = getPhotoById(PhotoEndpoint.VALID_PHOTO_ID)
                .statusCode(StatusCodes.OK)
                .header(PhotoEndpoint.HEADER_CONTENT_TYPE, containsString("application/json"))
                .body(PhotoPayload.photoSchema())
                .body("id", equalTo(PhotoEndpoint.VALID_PHOTO_ID))
                .body("albumId", notNullValue())
                .body("title", not(emptyOrNullString()))
                .body("url", not(emptyOrNullString()))
                .body("thumbnailUrl", not(emptyOrNullString()))
                .extract().response();

        PhotoPayload returned = response.as(PhotoPayload.class);
        assertThat(returned.getId()).as("id must match path param").isEqualTo(PhotoEndpoint.VALID_PHOTO_ID);
        assertThat(returned.getUrl()).as("url must not be blank").isNotBlank();
        assertThat(returned.getThumbnailUrl()).as("thumbnailUrl must not be blank").isNotBlank();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3 – GET /photos/{id} → 404 (negative)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Get single photo – not found")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("GET /photos/99999 → 404 for non-existent resource (negative test)")
    @TmsLink("TC-033")
    void getPhotoById_invalidId_returns404() {
        getPhotoById(PhotoEndpoint.INVALID_PHOTO_ID)
                .statusCode(StatusCodes.NOT_FOUND);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4 – GET /albums/{albumId}/photos (nested resource)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Get photos by album")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("GET /albums/1/photos → 200, exactly 50 photos all belonging to albumId=1")
    @TmsLink("TC-034")
    void getPhotosByAlbumId_returns50PhotosForAlbum1() {
        getPhotosByAlbumId(PhotoEndpoint.VALID_ALBUM_ID)
                .statusCode(StatusCodes.OK)
                .body("$", hasSize(PhotoEndpoint.EXPECTED_PHOTOS_PER_ALBUM))
                .body("albumId", everyItem(equalTo(PhotoEndpoint.VALID_ALBUM_ID)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5 – POST /photos (create)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Create new photo")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("POST /photos → 201, echoes sent fields, server assigns new id")
    @TmsLink("TC-035")
    void createPhoto_validPayload_returns201WithEchoedData() {
        PhotoPayload newPhoto = PhotoPayload.builder()
                .albumId(PhotoDataProvider.randomAlbumId())
                .title(PhotoDataProvider.randomPhotoTitle())
                .url(PhotoDataProvider.randomPhotoUrl())
                .thumbnailUrl(PhotoDataProvider.randomThumbnailUrl())
                .build();

        var response = createPhoto(newPhoto)
                .statusCode(StatusCodes.CREATED)
                .header(PhotoEndpoint.HEADER_CONTENT_TYPE, containsString("application/json"))
                .body("title", equalTo(newPhoto.getTitle()))
                .body("albumId", equalTo(newPhoto.getAlbumId()))
                .body("id", notNullValue())
                .extract().response();

        PhotoPayload created = response.as(PhotoPayload.class);
        assertThat(created.getTitle()).as("Returned title must match sent title").isEqualTo(newPhoto.getTitle());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6 – PUT /photos/{id} (full update)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Update photo")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("PUT /photos/1 → 200, updated title reflected in response")
    @TmsLink("TC-036")
    void updatePhoto_validPayload_returns200WithUpdatedTitle() {
        PhotoPayload updated = PhotoPayload.builder()
                .id(PhotoEndpoint.VALID_PHOTO_ID)
                .albumId(1)
                .title(PhotoDataProvider.randomPhotoTitle())
                .url(PhotoDataProvider.randomPhotoUrl())
                .thumbnailUrl(PhotoDataProvider.randomThumbnailUrl())
                .build();

        updatePhoto(PhotoEndpoint.VALID_PHOTO_ID, updated)
                .statusCode(StatusCodes.OK)
                .body("title", equalTo(updated.getTitle()))
                .body("id", equalTo(PhotoEndpoint.VALID_PHOTO_ID));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7 – PATCH /photos/{id} (partial update)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Partially update photo")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("PATCH /photos/1 → 200, only the patched title changes")
    @TmsLink("TC-037")
    void patchPhoto_titleOnly_returns200WithPatchedTitle() {
        String patchedTitle = "Patched: " + PhotoDataProvider.randomPhotoTitle();
        PhotoPayload partial = PhotoPayload.builder().title(patchedTitle).build();

        patchPhoto(PhotoEndpoint.VALID_PHOTO_ID, partial)
                .statusCode(StatusCodes.OK)
                .body("title", equalTo(patchedTitle));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8 – DELETE /photos/{id}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Delete photo")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("DELETE /photos/1 → 200, response body is empty object {}")
    @TmsLink("TC-038")
    void deletePhoto_validId_returns200() {
        deletePhoto(PhotoEndpoint.VALID_PHOTO_ID)
                .statusCode(StatusCodes.OK)
                .body("$", anEmptyMap());
    }
}
