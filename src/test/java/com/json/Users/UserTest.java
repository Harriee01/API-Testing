package com.json.Users;

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
 * UserTest – integration tests for the {@code /users} resource.
 *
 * <p>
 * <b>Coverage:</b>
 * <ol>
 * <li>GET /users → 200, ≥10 items, schema, content-type</li>
 * <li>GET /users/{id} → 200, all mandatory fields, POJO check</li>
 * <li>GET /users/{id} → 404 for non-existent id (negative)</li>
 * <li>GET /users → every email contains '@' (data-integrity)</li>
 * </ol>
 */
@Epic("JSONPlaceholder API")
@Feature("Users Resource")
@DisplayName("GET /users & /users/{id} Tests")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class UserTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(UserTest.class);

    // ─────────────────────────────────────────────────────────────────────────
    // HTTP Step Helpers
    // ─────────────────────────────────────────────────────────────────────────

    @Step("GET all users")
    private ValidatableResponse getAllUsers() {
        log.info("Step: GET {}", UserEndpoint.USERS);
        return given().spec(requestSpec)
                .when().get(UserEndpoint.USERS)
                .then().spec(buildResponseSpec());
    }

    @Step("GET user by id={userId}")
    private ValidatableResponse getUserById(int userId) {
        log.info("Step: GET /users/{}", userId);
        return given().spec(requestSpec)
                .pathParam("id", userId)
                .when().get(UserEndpoint.USER_BY_ID)
                .then().spec(buildResponseSpec());
    }

    private ResponseSpecification buildResponseSpec() {
        return new ResponseSpecBuilder()
                .expectResponseTime(lessThan(UserEndpoint.MAX_RESPONSE_TIME_MS))
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1 – GET /users (collection)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Get all users")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("GET /users → 200 with ≥10 users, correct Content-Type and valid schema")
    @TmsLink("TC-010")
    void getAllUsers_returns200WithAtLeast10Users() {
        getAllUsers()
                .statusCode(StatusCodes.OK)
                .header(UserEndpoint.HEADER_CONTENT_TYPE, containsString("application/json"))
                .body("$", hasSize(greaterThanOrEqualTo(UserEndpoint.EXPECTED_MIN_USER_COUNT)))
                .body("[0].id", notNullValue())
                .body("[0].name", not(emptyOrNullString()))
                .body("[0].username", not(emptyOrNullString()))
                .body("[0].email", not(emptyOrNullString()))
                .body(UserPayload.userSchema());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2 – GET /users/{id} (happy path)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Get single user")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("GET /users/1 → 200 with all mandatory fields and valid schema")
    @TmsLink("TC-011")
    void getUserById_validId_returns200WithAllMandatoryFields() {
        var response = getUserById(UserEndpoint.VALID_USER_ID)
                .statusCode(StatusCodes.OK)
                .header(UserEndpoint.HEADER_CONTENT_TYPE, containsString("application/json"))
                .body(UserPayload.userSchema())
                .body("id", equalTo(UserEndpoint.VALID_USER_ID))
                .body("name", not(emptyOrNullString()))
                .body("username", not(emptyOrNullString()))
                .body("email", containsString("@"))
                .body("phone", not(emptyOrNullString()))
                .body("website", not(emptyOrNullString()))
                .extract().response();

        UserPayload returnedUser = response.as(UserPayload.class);

        assertThat(returnedUser.getId())
                .as("User id must match the requested path parameter")
                .isEqualTo(UserEndpoint.VALID_USER_ID);

        assertThat(returnedUser.getEmail())
                .as("User email must contain an @ symbol")
                .contains("@");

        assertThat(returnedUser.getName())
                .as("User name must not be blank")
                .isNotBlank();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3 – GET /users/{id} → 404 (negative)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Get single user – not found")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("GET /users/9999 → 404 for non-existent resource (negative test)")
    @TmsLink("TC-012")
    void getUserById_invalidId_returns404() {
        getUserById(UserEndpoint.INVALID_USER_ID)
                .statusCode(StatusCodes.NOT_FOUND);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4 – Data integrity: all users have non-blank emails
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Validate user data integrity")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("GET /users → every user's email field contains '@' (data-integrity check)")
    @TmsLink("TC-013")
    void getAllUsers_everyEmailContainsAtSign() {
        getAllUsers()
                .statusCode(StatusCodes.OK)
                .body("email", everyItem(containsString("@")));
    }
}
