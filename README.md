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

### Technologies
- Java 26
- Spring Boot 4.1.0
- Spring Data JPA
- H2 Database (for local development and testing)
- PostgreSQL (for Docker/production environment)
- Swagger/OpenAPI (for documentation)

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
   docker-compose up --build
   ```
3. The app will start using the `prod` profile with a PostgreSQL database. You can access it at `http://localhost:8080`.

### Running Tests
I wrote many tests to make sure everything works correctly. I have unit tests for services, repository tests, and integration tests for controllers.
To run all tests, use this command:
- On Windows: `gradlew.bat test`
- On Linux/Mac: `./gradlew test`

After the tests finish, you can see a nice HTML report here:
`build/reports/tests/test/index.html`

### Documentation
You can see all the API endpoints and test them using Swagger UI. Just go to this link after starting the app:
`http://localhost:8080/swagger-ui/index.html`

Hope you find this project useful!
