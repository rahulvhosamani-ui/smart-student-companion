# Smart Student Companion

Spring Boot 3.3, Java 17, Thymeleaf, Spring Security, and MySQL application.

## Run locally

1. Install Java 17 or later and start a local MySQL server.
2. Create a database named `student_companion_db` (or set `DB_URL` to another database).
3. Set `DB_USERNAME` and `DB_PASSWORD` in your terminal or IDE run configuration. The defaults use the local MySQL `root` account with an empty password; set `DB_PASSWORD` if your local account uses one.
4. Start the app from this folder:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

5. Open `http://localhost:8080/login` and create an account at `/register`.

The local profile is active by default. The registration flow stores BCrypt password hashes. Do not commit passwords, API keys, or other secrets.

## Deploy to Railway

Attach a Railway MySQL service. In the Spring Boot service variables, configure:

- `SPRING_PROFILES_ACTIVE=prod`
- `DB_URL=jdbc:mysql://<private-host>:<port>/<database>?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
- `DB_USERNAME=<Railway MySQL username>`
- `DB_PASSWORD=<Railway MySQL password>`
- `GEMINI_API_KEY=<key>` if AI features are used

Build with the included Dockerfile. The server listens on Railway's `PORT` value, with `8080` as the local default.

To create an initial student account automatically, set both `INITIAL_STUDENT_EMAIL` and `INITIAL_STUDENT_PASSWORD`. Remove those variables after the account is created if they are no longer needed.

The AI Notes generator and AI Assistant both require a valid, enabled Gemini API key. For local PowerShell runs, set `GEMINI_API_KEY` in the same terminal before starting the app. For Railway, add it as a service variable and redeploy after code changes. A missing key returns HTTP 503; invalid key/model, quota, and upstream Gemini errors return a short actionable explanation. Never put the key in source files or screenshots.

## Notes

- Login and registration use server-rendered Thymeleaf forms with CSRF protection enabled. Static-page API writes fetch and send the CSRF token automatically.
- Notes, assignments, attendance, GPA, reminders, AI notes, flashcards, and AI chat use database-backed endpoints. After a successful save, each page reloads its saved list or conversation in place.
- Attendance records default to the current month when none is supplied. Reminder dates use the same `dueDate` field as the backend.
- All student pages now include a working logout control that submits a CSRF-protected logout request.
- The old duplicate project copy, generated build output, Git metadata, and local run logs are excluded from the clean ZIP.
- The password-reset screen was only a nonfunctional placeholder and has been removed.
- Before public multi-user use, verify that every academic record is scoped to its owner. Authentication alone does not guarantee student-to-student data isolation.
