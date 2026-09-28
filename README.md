# Nihao Backend

Backend for the NiHao Urdu learning app and its admin portal. It stores Chinese courses (HSK), units, lessons, vocabulary, and exercises, and it tracks each learner’s progress. Admins manage content through a REST API and a built-in admin page. The mobile app uses a separate API.

## Stack

- Java 17
- Spring Boot 3.1
- Spring Security with JWT (stateless)
- Spring Data JPA and MySQL 8
- Flyway for schema changes
- springdoc OpenAPI (Swagger UI)

## Requirements

- JDK 17
- MySQL 8

Maven is included through the wrapper (`mvnw` / `mvnw.cmd`), so a separate Maven install is not required.

## Run locally

1. Create the database:

```sql
CREATE DATABASE nihao_urdu CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Local defaults in `application.properties` use MySQL on `localhost` with user `root`. Override them with environment variables when the database is somewhere else: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, and `APP_JWT_SECRET`.

3. For Google sign-in, set the `GOOGLE_WEB_CLIENT_ID` environment variable to the same web OAuth client ID used by the Android app.

4. Start the server:

```bat
mvnw.cmd spring-boot:run
```

On macOS or Linux:

```bash
./mvnw spring-boot:run
```

The app listens on port **8089** with context path **`/api`**.

| | |
|---|---|
| API base | http://localhost:8089/api |
| Admin page | http://localhost:8089/api/index.html |
| Swagger UI | http://localhost:8089/api/swagger-ui.html |
| OpenAPI JSON | http://localhost:8089/api/api-docs |

Flyway runs the scripts in `src/main/resources/db/migration` on startup. On an empty database it creates the core tables first, then the progress and Google sign-in changes. Hibernate does not alter tables (`spring.jpa.hibernate.ddl-auto=none`).

## Deploy on Vercel

`Dockerfile.vercel` in the project root is the file Vercel builds. MySQL does not run on Vercel, so create an empty hosted database named `nihao_urdu` (Aiven, Railway, or another MySQL host) and set these environment variables on the Vercel project:

| Variable | Purpose |
|---|---|
| `SPRING_DATASOURCE_URL` | JDBC URL of the hosted database, with SSL options your host requires |
| `SPRING_DATASOURCE_USERNAME` | Database user |
| `SPRING_DATASOURCE_PASSWORD` | Database password |
| `APP_JWT_SECRET` | Long random string used to sign tokens |

Vercel sets `PORT` itself. The public API is `https://<your-project>.vercel.app/api`.

On first launch the app seeds two users, and if the database has no courses it also seeds a sample **Basic Chinese (HSK 1)** course:

| Role | Email | Password |
|---|---|---|
| Admin | `admin@nihao-urdu.com` | `admin123` |
| Student | `student@nihao-urdu.com` | `student123` |

Change these passwords before using the app anywhere other than your own machine.

## Authentication

Admin login:

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "admin@nihao-urdu.com",
  "password": "admin123"
}
```

Mobile login, registration, and Google sign-in:

```http
POST /api/v1/api/login
POST /api/v1/api/register
POST /api/v1/api/auth/google
```

Protected routes expect:

```http
Authorization: Bearer <token>
```

Access tokens last 24 hours. Refresh tokens last 7 days.

## Admin API

These routes require an admin token, except login and the admin page.

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/auth/login` | Admin login |
| GET, POST | `/api/courses` | List or create courses |
| GET, PUT, DELETE | `/api/courses/{id}` | Read, update, or delete a course |
| PATCH | `/api/courses/{id}/status` | Change publish status |
| GET | `/api/units/course/{courseId}` | List units in a course |
| POST | `/api/units/course/{courseId}` | Create a unit |
| GET, PUT, DELETE | `/api/units/{id}` | Read, update, or delete a unit |
| PATCH | `/api/units/reorder` | Reorder units |
| GET | `/api/lessons/unit/{unitId}` | List lessons in a unit |
| POST | `/api/lessons/unit/{unitId}` | Create a lesson |
| GET, PUT, DELETE | `/api/lessons/{id}` | Read, update, or delete a lesson |
| GET | `/api/exercises/lesson/{lessonId}` | List exercises in a lesson |
| POST | `/api/exercises/lesson/{lessonId}` | Create an exercise |
| GET, PUT, DELETE | `/api/exercises/{id}` | Read, update, or delete an exercise |
| GET, POST | `/api/vocabulary` | List or create vocabulary |
| POST | `/api/vocabulary/bulk` | Create many vocabulary items |
| GET, PUT, DELETE | `/api/vocabulary/{id}` | Read, update, or delete a word |
| GET | `/api/users` | List users |
| GET, PUT, DELETE | `/api/users/{id}` | Read, update, or delete a user |
| PATCH | `/api/users/{id}/ban` | Ban or unban a user |
| PATCH | `/api/users/{id}/premium` | Set premium status |

Course list accepts `page`, `size`, `search`, `status`, `sortBy`, and `sortDirection`.

## Mobile API

Login, register, and Google sign-in are public. Everything else requires a learner token.

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/api/login` | Email and password login |
| POST | `/api/v1/api/register` | Create an account |
| POST | `/api/v1/api/auth/google` | Google sign-in |
| GET | `/api/v1/api/me` | Restore the current session |
| GET | `/api/v1/courses` | Course list for the learner |
| GET | `/api/v1/courses/{courseId}` | Course detail |
| GET | `/api/v1/courses/{courseId}/progress` | Course progress |
| GET | `/api/v1/courses/{courseId}/units` | Unit path (`limit`, `offset`) |
| GET | `/api/v1/units/{unitId}` | Unit detail |
| GET | `/api/v1/units/{unitId}/lessons` | Lessons in a unit |
| GET | `/api/v1/api/lessons/{lessonId}` | Lesson detail |
| GET | `/api/v1/lessons/{lessonId}/exercises` | Exercises in a lesson |
| POST | `/api/v1/api/lessons/{lessonId}/complete` | Mark a lesson complete and award progress |
| GET | `/api/v1/items/{itemId}` | Vocabulary item detail |
| GET | `/api/v1/api/vocabulary` | Vocabulary list (`hskLevel`, `search`) |
| GET | `/api/v1/league` | Learner league |

Progress covers XP, level, streak, crowns, and the learner’s place in a course, unit, and lesson.

## Seed scripts

These scripts add extra HSK 1 content. Start the server first. `seed_hsk1.py` calls the admin API and needs a course id set at the top of the file.

| Script | What it does |
|---|---|
| `seed-hsk1.ps1` | Seeds HSK 1 content through the API |
| `seed_hsk1.py` | Seeds units, lessons, exercises, and vocabulary |
| `seed_sentences.py` | Seeds example sentences |
| `seed_expand_unit1_path.py` | Expands the unit 1 learning path |
| `seed_expand_unit1_path.sql` | SQL version of that unit 1 expansion |

## Tests

```bat
mvnw.cmd test
```

Tests use an in-memory H2 database.

## Project layout

```
src/main/java/com/codeneticx/nihaobackend/
  config/        Security, JWT, CORS, and startup seed data
  controller/    Admin and mobile HTTP APIs
  dto/           Request and response objects
  model/         Course, Unit, Lesson, Vocabulary, Exercise, User
  repository/    Spring Data JPA repositories
  service/       Auth, content, and progress logic
src/main/resources/
  application.properties
  db/migration/  Flyway scripts
  static/        Admin page
```
