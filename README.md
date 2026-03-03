# JSONPlaceholder REST Assured API Automation Suite

[![CI – API Tests](https://github.com/<YOUR_ORG>/Automated-API-Testing/actions/workflows/api-tests.yml/badge.svg)](https://github.com/<YOUR_ORG>/Automated-API-Testing/actions/workflows/api-tests.yml)
[![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![REST Assured](https://img.shields.io/badge/REST%20Assured-5.5.0-brightgreen)](https://rest-assured.io/)
[![Allure](https://img.shields.io/badge/Allure-2.29.0-orange)](https://allurereport.org/)

---

## 📋 Project Goal

Production-grade API automation framework that validates the public
[JSONPlaceholder](https://jsonplaceholder.typicode.com) REST API.

The project demonstrates **SOLID principles**, a clean layered architecture,
Allure reporting, JSON Schema validation, and full CI/CD integration via GitHub
Actions — ready to serve as a reference implementation or interview showcase.

---

## 🏗️ Project Structure

```
Automated-API-Testing/
├── .github/
│   └── workflows/
│       └── api-tests.yml           ← GitHub Actions CI pipeline
├── src/
│   ├── main/
│   │   └── resources/
│   │       ├── logback-test.xml    ← Logback config (test classpath)
│   │       └── schemas/
│   │           ├── post-schema.json
│   │           ├── post-array-schema.json
│   │           └── user-schema.json
│   └── test/
│       └── java/com/json/
│           ├── base/
│           │   └── BaseTest.java           ← Shared RequestSpecification
│           ├── constants/
│           │   ├── ApiConstants.java       ← Base URI, endpoints, timeouts
│           │   └── StatusCodes.java        ← HTTP status code constants
│           ├── models/
│           │   ├── Post.java               ← Post POJO (Lombok + Jackson)
│           │   └── User.java               ← User POJO (Lombok + Jackson)
│           ├── steps/
│           │   ├── PostSteps.java          ← @Step-annotated /posts operations
│           │   └── UserSteps.java          ← @Step-annotated /users operations
│           ├── tests/
│           │   ├── PostTests.java          ← 9 tests for /posts resource
│           │   └── UserTests.java          ← 4 tests for /users resource
│           └── utils/
│               ├── SchemaLoader.java       ← JSON Schema classpath resolver
│               └── TestDataGenerator.java  ← DataFaker-based random data
├── Dockerfile                      ← Multi-stage Docker image
├── docker-compose.yml              ← Run tests + Allure server via Docker
├── pom.xml                         ← Maven build descriptor
└── README.md
```

---

## ✅ Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| Java JDK | 21 (LTS) | [Download OpenJDK](https://adoptium.net/) |
| Maven | 3.9+ | Bundled via `.mvn/wrapper/` |
| Allure CLI | 2.29.0 | `brew install allure` or [download](https://allurereport.org/docs/install/) |
| Docker | 20+ | _Optional_ – for containerised test runs |

> **Windows users:** Replace `./mvnw` with `mvnw.cmd` in all commands below.

---

## 🚀 Running Tests Locally

### 1. Clone & enter the project

```bash
git clone https://github.com/<YOUR_ORG>/Automated-API-Testing.git
cd Automated-API-Testing
```

### 2. Run all tests

```bash
mvn clean test
```

### 3. Run a specific test class

```bash
mvn clean test -Dtest=PostTests
```

### 4. Run with Maven Wrapper (no global Maven required)

```bash
./mvnw clean test        # Linux / macOS
mvnw.cmd clean test      # Windows
```

---

## 📊 Viewing the Allure Report

### Option A – Allure CLI (recommended)

```bash
# Serve the interactive HTML report in your browser
allure serve target/allure-results

# Generate a static report into target/allure-report/
allure generate target/allure-results --clean -o target/allure-report
```

### Option B – Maven plugin

```bash
mvn allure:serve       # opens browser directly
mvn allure:report      # generates static HTML
```

---

## 🐳 Docker Usage

### Build and run tests inside a container

```bash
# Build the Docker image
docker build -t api-tests .

# Run tests (results are copied out via a volume mount)
docker run --rm -v "$(pwd)/target:/app/target" api-tests
```

### Docker Compose (tests + Allure UI server)

```bash
# Run tests and launch the Allure UI on http://localhost:5050
docker-compose up --build

# Tear down
docker-compose down
```

---

## 🤖 CI/CD (GitHub Actions)

The pipeline (`.github/workflows/api-tests.yml`) triggers on every push and
pull request to `main`:

1. Checks out the code
2. Sets up JDK 21
3. Runs `mvn clean test`
4. Uploads `target/allure-results` as a downloadable CI artefact
5. Generates and deploys the Allure HTML report to **GitHub Pages**

> Configure GitHub Pages in **Settings → Pages → Source: GitHub Actions** to
> enable the automatic report deployment step.

---

## 🧪 Test Coverage Summary

| # | Method | Endpoint | Scenario | Expected |
|---|--------|----------|----------|----------|
| TC-001 | GET | `/posts` | All posts, schema, size | 200 · ≥100 items |
| TC-002 | GET | `/posts/1` | Single post, field values, POJO | 200 |
| TC-003 | GET | `/posts/9999` | Non-existent id | 404 |
| TC-004 | POST | `/posts` | Create post, echoed fields | 201 · id=101 |
| TC-005 | POST | `/posts` | Empty title (documented quirk) | 201 |
| TC-006 | PUT | `/posts/1` | Full update, fields reflected | 200 |
| TC-007 | PATCH | `/posts/1` | Partial update (title only) | 200 |
| TC-008 | DELETE | `/posts/1` | Delete, empty body | 200 |
| TC-009 | GET | `/posts?userId=1` | Filter by query param | 200 · 10 items |
| TC-010 | GET | `/users` | All users, schema, size | 200 · ≥10 items |
| TC-011 | GET | `/users/1` | Single user, email format, POJO | 200 |
| TC-012 | GET | `/users/9999` | Non-existent id | 404 |
| TC-013 | GET | `/users` | Every email contains `@` | 200 |

---

## 🔰 SOLID Principles Applied

| Principle | How |
|-----------|-----|
| **S**ingle Responsibility | `BaseTest` = setup only; `XxxSteps` = HTTP only; `XxxTests` = assertions only |
| **O**pen/Closed | `@JsonIgnoreProperties(ignoreUnknown=true)` on models – add API fields without touching tests |
| **L**iskov Substitution | All test classes extend `BaseTest` without breaking the contract |
| **I**nterface Segregation | `PostSteps` & `UserSteps` are separate classes – consumers depend only on what they need |
| **D**ependency Inversion | `XxxSteps` depend on `RequestSpecification` (abstraction) injected via constructor |

---

## 📬 Allure Annotations Used

| Annotation | Purpose |
|------------|---------|
| `@Epic` | Groups features under the "JSONPlaceholder API" epic |
| `@Feature` | Groups tests by resource (Posts / Users) |
| `@Story` | Individual user story per test scenario |
| `@Severity` | `BLOCKER` / `CRITICAL` / `NORMAL` triage |
| `@TmsLink` | Links to test management system ticket |
| `@Issue` | Links to known bug / limitation |
| `@Step` | Named steps in PostSteps / UserSteps appear in Allure timeline |
| `@Attachment` | Request bodies attached to Allure report |

---

## 📝 License

MIT — see [LICENSE](LICENSE).
