# Smart Student Companion

Spring Boot 3.3 / Java 17 / MySQL application with static study pages and Thymeleaf login/registration pages.

## Folder structure

- `src/main/java/com/example/demo/` — controllers, entities, repositories, services and security.
- `src/main/resources/static/` — dashboard and study feature pages.
- `src/main/resources/templates/` — `login.html` and `register.html`.
- `src/main/resources/application.properties` — environment-based configuration.

## Run locally

1. Install JDK 17+ and Maven (or use `mvnw` after Maven Wrapper downloads successfully).
2. Create a MySQL database named `student_companion_db` and a database user.
3. Set environment variables in your terminal before starting:
   - `DB_URL=jdbc:mysql://localhost:3306/student_companion_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true`
   - `DB_USERNAME=<your MySQL username>`
   - `DB_PASSWORD=<your MySQL password>`
   - optionally `GEMINI_API_KEY=<your Gemini API key>`
4. Run `mvn spring-boot:run` and open `http://localhost:8081/login`.
5. Register a new student at `http://localhost:8081/register`.

The application no longer contains a hard-coded default student password. To optionally create a first account, set both `INITIAL_STUDENT_EMAIL` and `INITIAL_STUDENT_PASSWORD` environment variables before starting.

## Railway deployment

Attach a Railway MySQL service. In the Java service Variables, set:

- `SPRING_PROFILES_ACTIVE=prod`
- `DB_URL` to the MySQL JDBC URL using Railway's private host, database, and port
- `DB_USERNAME` to the Railway MySQL username
- `DB_PASSWORD` to the Railway MySQL password
- `GEMINI_API_KEY` if AI features are used

The app uses `${PORT:8081}` for the web port. Never commit real passwords or API keys to GitHub.

## Security and data notes

- Login uses Spring Security form login at `/perform_login` and the `/login` Thymeleaf template.
- CSRF protection is enabled. Browser fetch wrappers send the `XSRF-TOKEN` cookie as the `X-XSRF-TOKEN` header for API requests.
- All `/api/**` endpoints require an authenticated session.
- Before public use, add owner/student scoping to every academic record entity and repository; the existing data entities in this project currently store records globally rather than associating every note/assignment/attendance/GPA/reminder with an individual student.

## Build

Run `mvn clean package -DskipTests`. Tests are skipped by that command. This archive was not build-verified in this environment because Maven could not be downloaded here; please run the build locally and share any first `[ERROR]` section if it fails.
