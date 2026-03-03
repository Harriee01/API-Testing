package com.json.base;

import com.json.constants.ApiConstants;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.lessThan;

/**
 * BaseTest – the single, shared foundation for every test class.
 *
 * <p>
 * <b>Responsibilities (Single Responsibility Principle):</b>
 * <ul>
 * <li>Build and expose the {@link RequestSpecification} used by all tests</li>
 * <li>Build and expose the {@link ResponseSpecification} for common
 * assertions</li>
 * <li>Register logging and Allure filters once, globally</li>
 * </ul>
 *
 * <p>
 * <b>SOLID compliance:</b>
 * <ul>
 * <li><b>S</b> – Only manages shared setup; no test logic lives here</li>
 * <li><b>O</b> – Subclasses extend behaviour via @BeforeEach without touching
 * this class</li>
 * <li><b>D</b> – Test classes depend on the abstraction (RequestSpecification),
 * not a
 * concrete HTTP client implementation</li>
 * </ul>
 */
public abstract class BaseTest {

        /**
         * SLF4J logger – each subclass gets its own via
         * LoggerFactory.getLogger(getClass())
         */
        protected static final Logger log = LoggerFactory.getLogger(BaseTest.class);

        /**
         * Shared RequestSpecification – built once per JVM (static), then reused by
         * every
         * test method via given().spec(requestSpec). This eliminates duplication and
         * ensures
         * that changing the base URL or default headers requires only a single edit
         * here.
         */
        protected static RequestSpecification requestSpec;

        /**
         * Shared ResponseSpecification – enforces cross-cutting assertions (status
         * family,
         * response time) that every successful response must satisfy.
         * Individual tests may add further assertions on top of this.
         */
        protected static ResponseSpecification responseSpec;

        /**
         * One-time test-suite setup executed before any test method in any subclass.
         *
         * <p>
         * Static scope is intentional: the RequestSpecification is stateless and
         * thread-safe (REST Assured clones it per request), so building it once is safe
         * even in parallel test execution scenarios.
         */
        @BeforeAll
        static void globalSetUp() {
                log.info("Initialising BaseTest – building shared RequestSpecification");

                requestSpec = new RequestSpecBuilder()
                                // Base URI: every test path will be appended to this
                                .setBaseUri(ApiConstants.BASE_URI)
                                // All requests send and accept JSON by default
                                .setContentType(ContentType.JSON)
                                .setAccept(ContentType.JSON)
                                // AllureRestAssured filter: captures every request and response and
                                // attaches them to the Allure report automatically – zero boilerplate
                                // in test classes. We use the built-in default FreeMarker templates
                                // that ship inside allure-rest-assured.jar (no custom .ftl files needed).
                                .addFilter(new AllureRestAssured())
                                // Log full request details to console (helpful during local debugging)
                                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                                // Log full response details to console
                                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                                .build();

                responseSpec = new ResponseSpecBuilder()
                                // Every response must arrive within MAX_RESPONSE_TIME_MS milliseconds
                                // (validates SLA; JSONPlaceholder is very fast so 3 s is generous)
                                .expectResponseTime(lessThan(ApiConstants.MAX_RESPONSE_TIME_MS))
                                .build();

                log.info("BaseTest initialisation complete – base URI: {}", ApiConstants.BASE_URI);
        }
}