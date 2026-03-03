package com.json.Base;

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
 * BaseTest – the single shared foundation for every test class across
 * all 6 resource packages (Posts, Comments, Albums, Photos, Todos, Users).
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
 * <li><b>O</b> – Subclasses extend behaviour via @BeforeEach without modifying
 * this class</li>
 * <li><b>D</b> – Test classes depend on the RequestSpecification abstraction,
 * not a concrete HTTP client implementation</li>
 * </ul>
 */
public abstract class BaseTest {

        /** SLF4J logger shared across all subclasses. */
        protected static final Logger log = LoggerFactory.getLogger(BaseTest.class);

        /**
         * Shared RequestSpecification – built once per JVM (static scope), then reused
         * by every test via {@code given().spec(requestSpec)}.
         * REST Assured clones it per request so it is safe for parallel execution.
         */
        protected static RequestSpecification requestSpec;

        /**
         * Shared ResponseSpecification – enforces the global response-time SLA.
         * Individual test methods add further status-code and body assertions on top.
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
}