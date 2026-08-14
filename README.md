# Library Management System

Welcome to my Library Management System project! I built this as a backend practice — it started simple, but over time I kept adding things and it turned into something I'm actually proud of. It's a REST API that helps a library manage their books, members, and loan activities.

### What is this project about?
This is a Spring Boot application with a clean layered architecture (Controller, Service, Repository). I used DTOs so the database entities are not directly exposed to the outside world. Over time I added caching, file storage, scheduled jobs, and email notifications on top of the core CRUD features.

### Main Features
- **Book Management:** Add, update, delete or find books. Pagination and sorting are supported for all book lists.
- **Member Management:** Register new members and manage their info.
- **Loan System:** Members can borrow books if they are available. The system tracks return dates and statuses (ACTIVE, RETURNED, OVERDUE).
- **Book Cover Images:** You can upload, download, or delete a cover image for any book. Images are stored on the filesystem under the `uploads/` directory, not in the database.
- **Caching:** I added Caffeine cache for books, authors, categories, and members. Cache is automatically invalidated on writes, so you always get fresh data. This noticeably reduces response times for repeated reads.
- **Scheduled Overdue Task:** Every night at midnight, a scheduled job automatically marks all past-due active loans as OVERDUE. The status update is done with a single bulk query, so there's no extra DB request on every API call to check if something is overdue.
- **Email Notifications:** When a member borrows a book, they get an automatic email confirmation. When a loan becomes overdue, they also get a reminder email. Emails are sent asynchronously using a `TaskExecutor` so they never slow down the main request.
- **Email Templates:** The emails look nice because I used Thymeleaf HTML templates for both the loan confirmation and the overdue reminder.
- **Advanced Book Endpoints:** Filter books by category or author, search by keyword, or use the dynamic search endpoint that accepts multiple filters at once (title, author name, category, availability). There are also stats endpoints like most borrowed books and top categories.
- **Advanced Loan Endpoints:** Filter loans by member, status, or book. Get overdue loan details, filter by member + status at the same time, or list all active loans for a specific book.
- **Validation:** Input validation is applied everywhere so the data stays correct.
- **Error Handling:** If something goes wrong, the API returns a clear and structured error message.
- **Authentication:** Users can register and log in with JWT tokens.
- **Role-Based Access:** USERs can view the catalog. ADMINs can manage books, authors, categories, members, and loans.

### Technologies
- Java 26
- Spring Boot 4.1.0
- Spring Data JPA
- H2 Database (for local development and testing)
- PostgreSQL (for Docker/production environment)
- Caffeine Cache (in-memory caching)
- JavaMailSender + MailHog (email sending and testing)
- Thymeleaf (HTML email templates)
- Spring Scheduler (scheduled tasks)
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

The default credentials are:

```
Email:    admin@admin.com
Password: admin
```

You can change these before starting the application by setting `INITIAL_ADMIN_EMAIL` and `INITIAL_ADMIN_PASSWORD` in your `.env` file. For production, always set a unique email address and a strong password before the first start.

### How to Run
It is very easy to run this project. You have two options:

#### Option 1: Using Gradle (Local)
1. Make sure you have Java 26 installed.
2. Open a terminal in the project folder and run:
   - On Windows: `gradlew.bat bootRun`
   - On Linux/Mac: `./gradlew bootRun`
3. The app will be ready at `http://localhost:8080`.

This option uses the `dev` profile with an H2 in-memory database. No PostgreSQL needed.

#### Option 2: Using Docker Compose
This is even easier because you don't need to install Java or PostgreSQL manually.
1. Make sure you have Docker installed.
2. Run this command:
   ```bash
   docker compose up --build
   ```
3. The app will start using the `prod` profile with a PostgreSQL database. You can access it at `http://localhost:8080`.

Docker Compose also starts a **MailHog** container, so you can actually test email notifications. When a loan is created or a book becomes overdue, the email lands in MailHog instead of a real inbox. Open the MailHog UI at `http://localhost:8025` to see the emails.

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

### Notable Endpoints

#### Book Endpoints
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/books` | List all books (paginated) |
| `GET` | `/api/books/{id}` | Get a single book |
| `POST` | `/api/books` | Create a book (ADMIN) |
| `PUT` | `/api/books/{id}` | Update a book (ADMIN) |
| `DELETE` | `/api/books/{id}` | Delete a book (ADMIN) |
| `GET` | `/api/books/search` | Search books by keyword |
| `GET` | `/api/books/search/dynamic` | Dynamic multi-filter search |
| `GET` | `/api/books/available` | List available books |
| `GET` | `/api/books/available/details` | Available books with full details |
| `GET` | `/api/books/category/{id}` | Books by category |
| `GET` | `/api/books/author/{id}` | Books by author |
| `GET` | `/api/books/never-borrowed` | Books that were never borrowed |
| `GET` | `/api/books/top-categories` | Category stats by book count |
| `GET` | `/api/books/most-borrowed` | Most borrowed books |
| `GET` | `/api/books/category/{id}/min-copies/{n}` | Books by category with at least N available copies |
| `POST` | `/api/books/{id}/cover` | Upload cover image (multipart) |
| `GET` | `/api/books/{id}/cover` | Download cover image |
| `DELETE` | `/api/books/{id}/cover` | Delete cover image |

#### Loan Endpoints
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/loans` | List all loans (paginated) |
| `GET` | `/api/loans/{id}` | Get a single loan |
| `POST` | `/api/loans` | Create a loan (borrow a book) |
| `PUT` | `/api/loans/{id}` | Update a loan |
| `DELETE` | `/api/loans/{id}` | Delete a loan |
| `PUT` | `/api/loans/{id}/return` | Return a borrowed book |
| `GET` | `/api/loans/active` | List all active loans |
| `GET` | `/api/loans/overdue` | List all overdue loans |
| `GET` | `/api/loans/overdue/details` | Overdue loans with detailed info |
| `GET` | `/api/loans/status/{status}` | Loans filtered by status |
| `GET` | `/api/loans/member/{id}` | Loans by member |
| `GET` | `/api/loans/member/{id}/status/{status}` | Loans by member + status |
| `GET` | `/api/loans/member/{id}/status/{status}/details` | Member loans with full details |
| `GET` | `/api/loans/book/{id}/active` | Active loans for a specific book |

### Documentation
You can see all the API endpoints and test them using Swagger UI. Just go to this link after starting the app:
`http://localhost:8080/swagger-ui/index.html`

Hope you find this project useful!
