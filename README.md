# Library Management System

Welcome to my Library Management System project! I created this application as a backend practice. It is a REST API that helps a library to manage their books, members, and loan activities.

### What is this project about?
This is a standard Spring Boot application. I used a layered architecture here (Controller, Service, Repository) to keep the code organized. Also, I used DTOs, so the database entities are not directly shown to the user.

### Main Features
- **Book Management:** You can add, update, delete or find books. There is also pagination and sorting for book lists.
- **Member Management:** You can register new members and manage their info.
- **Loan System:** This is the core part. Members can borrow books if they are available. The system also tracks return dates and statuses.
- **Validation:** I added validation for inputs so the data is always correct.
- **Error Handling:** If something goes wrong, the API returns a clear error message.
- **Authentication:** Users can register and log in with JWT tokens.
- **Role-Based Access:** USERs can view the catalog. ADMINs can manage books, authors, categories, members, and loans.

### Technologies
- Java 26
- Spring Boot 4.1.0
- Spring Data JPA
- H2 Database (for local development and testing)
- PostgreSQL (for Docker/production environment)
- Swagger/OpenAPI (for documentation)
- Spring Security
- JWT (JSON Web Token)

### Environment Configuration
The project uses a local `.env` file for Docker settings and JWT configuration. This file is ignored by Git because it contains secrets.

Add these values to your local `.env` file before running the application:

```text
JWT_SECRET=your-base64-encoded-secret-with-at-least-32-bytes
JWT_EXPIRATION_MS=3600000
INITIAL_ADMIN_EMAIL=your-admin-email
INITIAL_ADMIN_PASSWORD=your-strong-admin-password
```

`JWT_EXPIRATION_MS=3600000` means that a token is valid for one hour. Do not add real secrets or passwords to `application.yml` or commit the `.env` file to Git.

#### Initial admin account

When the application starts with no ADMIN user, it creates one account from `INITIAL_ADMIN_EMAIL` and `INITIAL_ADMIN_PASSWORD`. The password is stored with BCrypt. If an ADMIN already exists, no new default admin is created.

Use the values currently defined in your local `.env` file to log in as the initial admin. These login values are intentionally not written in this README because `.env` is local and ignored by Git. For production, set a unique email address and a strong password before the first application start.

### How to Run
It is very easy to run this project. You have two options:

#### Option 1: Using Gradle (Local)
1. Make sure you have Java 26 installed.
2. Open a terminal in the project folder and run:
   - On Windows: `gradlew.bat bootRun`
   - On Linux/Mac: `./gradlew bootRun`
3. The app will be ready at `http://localhost:8080`.

#### Option 2: Using Docker Compose
This is even easier because you don't need to install Java or PostgreSQL manually.
1. Make sure you have Docker installed.
2. Run this command:
   ```bash
   docker compose up --build
   ```
3. The app will start using the `prod` profile with a PostgreSQL database. You can access it at `http://localhost:8080`.

### Running Tests
I wrote many tests to make sure everything works correctly. I have unit tests for services, repository tests, and integration tests for controllers.
To run all tests, use this command:
- On Windows: `gradlew.bat test`
- On Linux/Mac: `./gradlew test`

After the tests finish, you can see a nice HTML report here:
`build/reports/tests/test/index.html`

### Authentication and Authorization

#### Register

```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

#### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

Both endpoints return a Bearer token. Use it on protected endpoints:

```http
Authorization: Bearer <token>
```

Access rules:

- USER and ADMIN can read books, authors, and categories.
- Only ADMIN can create, update, or delete books, authors, and categories.
- Only ADMIN can access member and loan endpoints.
- Missing or invalid tokens return `401 Unauthorized`.
- A valid token without the required role returns `403 Forbidden`.
- Expired tokens return `401 Unauthorized`.

### Documentation
You can see all the API endpoints and test them using Swagger UI. Just go to this link after starting the app:
`http://localhost:8080/swagger-ui/index.html`

Hope you find this project useful!
