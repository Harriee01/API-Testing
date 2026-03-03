package com.json.Todos;

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
 * TodoTest – integration tests for the {@code /todos} resource.
 *
 * <p>
 * <b>Coverage:</b>
 * <ol>
 * <li>GET /todos → 200, ≥200 items, content-type</li>
 * <li>GET /todos/{id} → 200, all fields present, schema, POJO check</li>
 * <li>GET /todos/{id} → 404 for non-existent id (negative)</li>
 * <li>GET /users/{userId}/todos → 200, 20 todos for user 1</li>
 * <li>GET /todos?completed=false → filters to incomplete todos only</li>
 * <li>POST /todos → 201, echoed fields</li>
 * <li>PUT /todos/{id} → 200, updated fields reflected</li>
 * <li>PATCH /todos/{id} → 200, partial update (completed flag) reflected</li>
 * <li>DELETE /todos/{id} → 200, empty response body</li>
 * </ol>
 */
@Epic("JSONPlaceholder API")
@Feature("Todos Resource")
@DisplayName("/todos & /users/{userId}/todos Tests")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class TodoTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(TodoTest.class);

    // ─────────────────────────────────────────────────────────────────────────
    // HTTP Step Helpers
    // ─────────────────────────────────────────────────────────────────────────

    @Step("GET all todos")
    private ValidatableResponse getAllTodos() {
        log.info("Step: GET {}", TodoEndpoint.TODOS);
        return given().spec(requestSpec)
                .when().get(TodoEndpoint.TODOS)
                .then().spec(buildResponseSpec());
    }

    @Step("GET todo by id={todoId}")
    private ValidatableResponse getTodoById(int todoId) {
        log.info("Step: GET /todos/{}", todoId);
        return given().spec(requestSpec)
                .pathParam("id", todoId)
                .when().get(TodoEndpoint.TODO_BY_ID)
                .then().spec(buildResponseSpec());
    }

    @Step("GET todos for user id={userId}")
    private ValidatableResponse getTodosByUserId(int userId) {
        log.info("Step: GET /users/{}/todos", userId);
        return given().spec(requestSpec)
                .pathParam("userId", userId)
                .when().get(TodoEndpoint.USER_TODOS)
                .then().spec(buildResponseSpec());
    }

    @Step("GET todos filtered by completed={completed}")
    private ValidatableResponse getTodosByCompleted(boolean completed) {
        log.info("Step: GET /todos?completed={}", completed);
        return given().spec(requestSpec)
                .queryParam("completed", completed)
                .when().get(TodoEndpoint.TODOS)
                .then().spec(buildResponseSpec());
    }

    @Step("POST new todo")
    private ValidatableResponse createTodo(TodoPayload todo) {
        log.info("Step: POST /todos – title: '{}'", todo.getTitle());
        return given().spec(requestSpec)
                .body(todo)
                .when().post(TodoEndpoint.TODOS)
                .then().spec(buildResponseSpec());
    }

    @Step("PUT todo id={todoId} with updated data")
    private ValidatableResponse updateTodo(int todoId, TodoPayload updated) {
        log.info("Step: PUT /todos/{}", todoId);
        return given().spec(requestSpec)
                .pathParam("id", todoId)
                .body(updated)
                .when().put(TodoEndpoint.TODO_BY_ID)
                .then().spec(buildResponseSpec());
    }

    @Step("PATCH todo id={todoId} with partial data")
    private ValidatableResponse patchTodo(int todoId, TodoPayload partial) {
        log.info("Step: PATCH /todos/{}", todoId);
        return given().spec(requestSpec)
                .pathParam("id", todoId)
                .body(partial)
                .when().patch(TodoEndpoint.TODO_BY_ID)
                .then().spec(buildResponseSpec());
    }

    @Step("DELETE todo id={todoId}")
    private ValidatableResponse deleteTodo(int todoId) {
        log.info("Step: DELETE /todos/{}", todoId);
        return given().spec(requestSpec)
                .pathParam("id", todoId)
                .when().delete(TodoEndpoint.TODO_BY_ID)
                .then().spec(buildResponseSpec());
    }

    private ResponseSpecification buildResponseSpec() {
        return new ResponseSpecBuilder()
                .expectResponseTime(lessThan(TodoEndpoint.MAX_RESPONSE_TIME_MS))
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1 – GET /todos (collection)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Get all todos")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("GET /todos → 200 with ≥200 items and correct Content-Type")
    @TmsLink("TC-039")
    void getAllTodos_returns200WithAtLeast200Todos() {
        getAllTodos()
                .statusCode(StatusCodes.OK)
                .header(TodoEndpoint.HEADER_CONTENT_TYPE, containsString("application/json"))
                .body("$", hasSize(greaterThanOrEqualTo(TodoEndpoint.EXPECTED_MIN_TODO_COUNT)))
                .body("[0].userId", notNullValue())
                .body("[0].id", notNullValue())
                .body("[0].title", not(emptyOrNullString()))
                .body("[0].completed", notNullValue());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2 – GET /todos/{id} (happy path)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Get single todo")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("GET /todos/1 → 200 with all mandatory fields and valid schema")
    @TmsLink("TC-040")
    void getTodoById_validId_returns200WithAllFields() {
        var response = getTodoById(TodoEndpoint.VALID_TODO_ID)
                .statusCode(StatusCodes.OK)
                .header(TodoEndpoint.HEADER_CONTENT_TYPE, containsString("application/json"))
                .body(TodoPayload.todoSchema())
                .body("id", equalTo(TodoEndpoint.VALID_TODO_ID))
                .body("userId", notNullValue())
                .body("title", not(emptyOrNullString()))
                .body("completed", notNullValue())
                .extract().response();

        TodoPayload returned = response.as(TodoPayload.class);
        assertThat(returned.getId()).as("id must match path param").isEqualTo(TodoEndpoint.VALID_TODO_ID);
        assertThat(returned.getTitle()).as("title must not be blank").isNotBlank();
        assertThat(returned.getCompleted()).as("completed must not be null").isNotNull();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3 – GET /todos/{id} → 404 (negative)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Get single todo – not found")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("GET /todos/9999 → 404 for non-existent resource (negative test)")
    @TmsLink("TC-041")
    void getTodoById_invalidId_returns404() {
        getTodoById(TodoEndpoint.INVALID_TODO_ID)
                .statusCode(StatusCodes.NOT_FOUND);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4 – GET /users/{userId}/todos (nested resource)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Get todos by user")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("GET /users/1/todos → 200, exactly 20 todos all belonging to userId=1")
    @TmsLink("TC-042")
    void getTodosByUserId_returns20TodosForUser1() {
        getTodosByUserId(TodoEndpoint.VALID_USER_ID)
                .statusCode(StatusCodes.OK)
                .body("$", hasSize(TodoEndpoint.EXPECTED_TODOS_PER_USER))
                .body("userId", everyItem(equalTo(TodoEndpoint.VALID_USER_ID)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5 – GET /todos?completed=false (query filter)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Filter todos by completion status")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("GET /todos?completed=false → 200, every returned todo is incomplete")
    @TmsLink("TC-043")
    void getTodosByCompleted_false_returnsOnlyIncompleteTodos() {
        getTodosByCompleted(false)
                .statusCode(StatusCodes.OK)
                .body("completed", everyItem(equalTo(false)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6 – POST /todos (create)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Create new todo")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("POST /todos → 201, echoes sent fields, server assigns new id")
    @TmsLink("TC-044")
    void createTodo_validPayload_returns201WithEchoedData() {
        TodoPayload newTodo = TodoPayload.builder()
                .userId(TodoDataProvider.randomUserId())
                .title(TodoDataProvider.randomTodoTitle())
                .completed(false)
                .build();

        var response = createTodo(newTodo)
                .statusCode(StatusCodes.CREATED)
                .header(TodoEndpoint.HEADER_CONTENT_TYPE, containsString("application/json"))
                .body("title", equalTo(newTodo.getTitle()))
                .body("userId", equalTo(newTodo.getUserId()))
                .body("completed", equalTo(newTodo.getCompleted()))
                .body("id", notNullValue())
                .extract().response();

        TodoPayload created = response.as(TodoPayload.class);
        assertThat(created.getTitle()).as("Returned title must match sent title").isEqualTo(newTodo.getTitle());
        assertThat(created.getCompleted()).as("Returned completed must match sent value").isFalse();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7 – PUT /todos/{id} (full update)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Update todo")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("PUT /todos/1 → 200, updated title and completed flag reflected in response")
    @TmsLink("TC-045")
    void updateTodo_validPayload_returns200WithUpdatedFields() {
        TodoPayload updated = TodoPayload.builder()
                .id(TodoEndpoint.VALID_TODO_ID)
                .userId(1)
                .title(TodoDataProvider.randomTodoTitle())
                .completed(true)
                .build();

        updateTodo(TodoEndpoint.VALID_TODO_ID, updated)
                .statusCode(StatusCodes.OK)
                .body("title", equalTo(updated.getTitle()))
                .body("completed", equalTo(true))
                .body("id", equalTo(TodoEndpoint.VALID_TODO_ID));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8 – PATCH /todos/{id} (partial update)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Partially update todo")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("PATCH /todos/1 → 200, completed flag toggled to true")
    @TmsLink("TC-046")
    void patchTodo_completedFlag_returns200WithToggledStatus() {
        TodoPayload partial = TodoPayload.builder().completed(true).build();

        patchTodo(TodoEndpoint.VALID_TODO_ID, partial)
                .statusCode(StatusCodes.OK)
                .body("completed", equalTo(true));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 9 – DELETE /todos/{id}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Delete todo")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("DELETE /todos/1 → 200, response body is empty object {}")
    @TmsLink("TC-047")
    void deleteTodo_validId_returns200() {
        deleteTodo(TodoEndpoint.VALID_TODO_ID)
                .statusCode(StatusCodes.OK)
                .body("$", anEmptyMap());
    }
}
