# Smart Student Companion

A Spring Boot 3.3 application for student assignments, attendance, GPA, notes, reminders, flashcards, and AI study tools.

## Requirements

- Java 17 or newer
- MySQL 8.x
- A Gemini API key only if you want to use AI features

The Maven wrapper is included; a separate Maven installation is not required.

## Local setup (Windows PowerShell)

1. Create a MySQL database and a dedicated local user. Avoid using your MySQL administrator account for the app:

   ```sql
   CREATE DATABASE student_companion_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'student_app'@'localhost' IDENTIFIED BY 'choose-a-local-password';
   GRANT ALL PRIVILEGES ON student_companion_db.* TO 'student_app'@'localhost';
   ```

2. Set the connection settings in the terminal you will use to run the app:

   ```powershell
   $env:DB_URL = 'jdbc:mysql://localhost:3306/student_companion_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
   $env:DB_USERNAME = 'student_app'
   $env:DB_PASSWORD = 'your-local-password'
   # Optional, only needed for AI features:
   $env:GEMINI_API_KEY = 'your-key'
   ```

3. Start the application:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

4. Open `http://localhost:8080/register` to create an account, then sign in at `/login`.

The local profile is selected by default. It has no embedded database password. Set `DB_PASSWORD` in your environment when your MySQL user requires one. Do not put live passwords or API keys in source files, screenshots, or committed `.env` files.

## Tests

Run the automated controller tests with (the suite uses an isolated in-memory H2 database, so a local MySQL server is not needed):

```powershell
.\mvnw.cmd test
```

The suite covers GPA creation, duplicate semester rejection, update by database ID, owner restricted deletion, and student scoped list queries for the other academic record APIs.

## Database migrations and existing data

Flyway records applied SQL migrations under `src/main/resources/db/migration`. Version 1 adds student ownership columns to older installations and changes GPA uniqueness from global semester uniqueness to per student. Hibernate updates the remaining entity schema during startup.

Older records without an owner are deliberately hidden from student APIs. The app cannot safely guess which account owns old shared records. Before upgrading, make a database backup. If you need to preserve legacy records, review them and assign each to a verified student ID before making them visible; do not bulk assign records without checking ownership. New records are always saved against the authenticated account, and read, update, and delete queries are scoped to that account.

## Backup and restore

From PowerShell, set the database name and use `mysqldump` to create a backup. MySQL prompts for the password so it is not stored in shell history:

```powershell
mysqldump --host=localhost --user=student_app --single-transaction --routines --triggers --result-file=student_companion_backup.sql student_companion_db
```

Restore into a database after confirming the target and retaining a separate copy of the backup:

```powershell
mysql --host=localhost --user=student_app student_companion_db < student_companion_backup.sql
```

Store backups outside the repository in a restricted location. Never add database dumps to Git or the project ZIP.

## Production deployment (Railway)

Attach a MySQL service and configure these environment variables in the application service:

- `SPRING_PROFILES_ACTIVE=prod`
- `DB_URL` using the private MySQL hostname and port
- `DB_USERNAME`
- `DB_PASSWORD`
- `GEMINI_API_KEY` if AI features are enabled

The included Dockerfile builds the application. The server uses Railway's `PORT` value, with `8080` as the local default. Configure backups for the production database before deployment and retain a tested restore path.

To create an initial student account automatically, set both `INITIAL_STUDENT_EMAIL` and `INITIAL_STUDENT_PASSWORD` as deployment secrets. Remove them after the account is created.

## Project hygiene

Generated build output, IDE files, Git metadata, logs, local environment files, ZIP files, and SQL backups are excluded from the packaged project. Review `.gitignore` before adding new local artifacts.
