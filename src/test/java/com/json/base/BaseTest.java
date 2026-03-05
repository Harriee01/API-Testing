package com.json.base;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.lessThan;

/**
 * BaseTest – the single shared foundation for every test class across
 * all 6 resource packages (Posts, Comments, Albums, Photos, Todos, Users).


 * Responsibilities (Single Responsibility Principle):

 * Build and expose the {@link RequestSpecification} used by all tests
 * Build and expose the {@link ResponseSpecification} for common
 * assertions
 * Register logging and Allure filters once, globally
 * SOLID compliance:
 *  – Only manages shared setup; no test logic lives here
 *  – Subclasses extend behaviour via @BeforeEach without modifying
 * this class<
 *  – Test classes depend on the RequestSpecification abstraction,
 * not a concrete HTTP client implementation
 */
public abstract class BaseTest {

        /** SLF4J logger shared across all subclasses. */
        protected static final Logger log = LoggerFactory.getLogger(BaseTest.class);

        /**
         * A pre-configured template for sending API requests. Every test uses this so all requests look and behave the same way
         * a type from REST Assured, representing a reusable specification for HTTP requests
         */
        protected static RequestSpecification requestSpec;

        /**
         * A template for validating API responses. Checks that all responses come back within 3 seconds
         */
        protected static ResponseSpecification responseSpec;

        /** Root URL for the JSONPlaceholder public API. Shared by all 6 resources. */
        private static final String BASE_URI = "https://jsonplaceholder.typicode.com";

        /** Maximum acceptable response time in milliseconds (3-second SLA). */
        private static final long MAX_RESPONSE_TIME_MS = 3_000L;

        /**
         * One-time test-suite setup – executed before any test method in any subclass.
         * Static scope is intentional: the specification is stateless and thread-safe.
         */

        //A setup method that runs once before any test starts, initializing the specs
        @BeforeAll
        static void globalSetUp() {
                log.info("Initialising BaseTest – building shared RequestSpecification for all resources");

                requestSpec = new RequestSpecBuilder()
                                .setBaseUri(BASE_URI)
                                .setContentType(ContentType.JSON)
                                .setAccept(ContentType.JSON)
                                // Attaches full request + response to Allure report automatically
                                .addFilter(new AllureRestAssured())
                                // Console logging for local debugging
                                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                                .build();

                responseSpec = new ResponseSpecBuilder()
                                .expectResponseTime(lessThan(MAX_RESPONSE_TIME_MS))
                                .build();

                log.info("BaseTest initialisation complete – base URI: {}", BASE_URI);
        }

        @AfterAll
        static void globalTearDown() {
                log.info("BaseTest teardown – no resources to clean up");
                RestAssured.reset();
        }
}