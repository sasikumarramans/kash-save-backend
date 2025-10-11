# Kash Save Backend - Requirements Document

## 1. Project Overview

**Project Name:** Kash Save Backend
**Project Type:** Personal Finance & Expense Management Platform
**Technology Stack:** Java 17, Spring Boot 3.5.5, PostgreSQL, JWT Authentication
**Architecture Pattern:** Clean Architecture (Domain-Driven Design)

### 1.1 Project Description
Kash Save is a comprehensive personal finance and expense management system that enables users to track their income and expenses, manage multiple financial books, split expenses with friends and groups, and generate detailed financial reports.

---

## 2. System Architecture

### 2.1 Architectural Layers

The application follows Clean Architecture with the following layers:

1. **Presentation Layer** (`presentation.controller`)
   - REST API Controllers
   - Request/Response DTOs
   - API endpoint definitions

2. **Use Case Layer** (`usecase.service`)
   - Business logic orchestration
   - Service implementations
   - Application-specific business rules

3. **Domain Layer** (`domain`)
   - Core business entities
   - Domain models
   - Domain services
   - Repository interfaces

4. **Infrastructure Layer** (`infrastructure`)
   - Database entities (JPA)
   - Repository implementations
   - Security configurations
   - External integrations

### 2.2 Technology Stack

- **Backend Framework:** Spring Boot 3.5.5
- **Language:** Java 17
- **Database:** PostgreSQL
- **ORM:** Spring Data JPA with Hibernate
- **Database Migration:** Liquibase
- **Security:** Spring Security with JWT
- **Documentation:** SpringDoc OpenAPI (Swagger)
- **PDF Generation:** iText7
- **Caching:** Spring Cache with Caffeine
- **Email:** Spring Mail
- **Build Tool:** Gradle
- **Testing:** JUnit, Spring Security Test, Testcontainers

---

## 3. Functional Requirements

### 3.1 User Management

#### 3.1.1 User Authentication & Authorization
- **FR-AUTH-001:** System shall support OTP-based authentication via mobile number
- **FR-AUTH-002:** System shall send 4-digit OTP to user's mobile number (currently static "1234" for development)
- **FR-AUTH-003:** System shall validate OTP with expiration time
- **FR-AUTH-004:** System shall implement rate limiting (max 5 OTP requests per hour per mobile number)
- **FR-AUTH-005:** System shall generate JWT access tokens with configurable expiration
- **FR-AUTH-006:** System shall generate refresh tokens with 30-day validity
- **FR-AUTH-007:** System shall support token refresh mechanism
- **FR-AUTH-008:** System shall support user logout (single device)
- **FR-AUTH-009:** System shall support logout from all devices
- **FR-AUTH-010:** System shall auto-create user account on first successful OTP verification

#### 3.1.2 User Profile Management
- **FR-USER-001:** System shall store user profile information (phone number, email, first name, last name, username, profile image)
- **FR-USER-002:** Users shall be able to update their profile information
- **FR-USER-003:** Users shall be able to upload profile images (max 5MB)
- **FR-USER-004:** System shall support user roles (CUSTOMER, ADMIN)
- **FR-USER-005:** System shall track user status (ACTIVE, INACTIVE, SUSPENDED)

### 3.2 Book Management (Financial Ledgers)

#### 3.2.1 Book Operations
- **FR-BOOK-001:** Users shall be able to create multiple financial books
- **FR-BOOK-002:** Each book shall have a name, optional description, and currency
- **FR-BOOK-003:** Users shall be able to view all their books with pagination
- **FR-BOOK-004:** Users shall be able to delete their books
- **FR-BOOK-005:** System shall display book summaries including total expense, total income, and last entry date
- **FR-BOOK-006:** Users can only access and manage their own books
- **FR-BOOK-007:** Default currency is INR (Indian Rupees)

### 3.3 Entry Management (Transactions)

#### 3.3.1 Entry Operations
- **FR-ENTRY-001:** Users shall be able to create income and expense entries
- **FR-ENTRY-002:** Each entry shall have: type (INCOME/EXPENSE), name, amount, currency, and date-time
- **FR-ENTRY-003:** Users shall be able to update existing entries
- **FR-ENTRY-004:** Users shall be able to delete entries
- **FR-ENTRY-005:** Users shall be able to view entries by book with pagination
- **FR-ENTRY-006:** Users can only manage entries in their own books
- **FR-ENTRY-007:** Amounts shall be stored with precision of 15 digits and 2 decimal places
- **FR-ENTRY-008:** Entry names can be up to 200 characters

### 3.4 Group Management (Expense Splitting)

#### 3.4.1 Group Operations
- **FR-GROUP-001:** Users shall be able to create groups with name, description, and currency
- **FR-GROUP-002:** Group creator becomes the admin by default
- **FR-GROUP-003:** Users shall be able to add members to groups by username
- **FR-GROUP-004:** Admin shall be able to remove members from groups
- **FR-GROUP-005:** Admin shall be able to promote members to admin
- **FR-GROUP-006:** Users shall be able to leave groups
- **FR-GROUP-007:** Admin shall be able to delete groups
- **FR-GROUP-008:** System shall track group creation date and member join dates
- **FR-GROUP-009:** Users shall be able to view all groups they are part of with pagination
- **FR-GROUP-010:** Users shall be able to update group details (name, description, currency)

### 3.5 Split Expense Management

#### 3.5.1 Split Expense Operations
- **FR-SPLIT-001:** Users shall be able to create split expenses within groups or individually
- **FR-SPLIT-002:** Split expenses shall support multiple split types (EQUAL, PERCENTAGE, EXACT)
- **FR-SPLIT-003:** Each split expense shall have: description, total amount, currency, paid by user, and participants
- **FR-SPLIT-004:** System shall automatically calculate split amounts based on split type:
  - **EQUAL:** Divide equally among participants
  - **PERCENTAGE:** Split based on percentage values
  - **EXACT:** Use exact amounts specified for each participant
- **FR-SPLIT-005:** Users shall be able to view all their split expenses with pagination
- **FR-SPLIT-006:** Users shall be able to view group-specific split expenses
- **FR-SPLIT-007:** Users shall be able to view individual (non-group) split expenses
- **FR-SPLIT-008:** Users shall be able to delete split expenses they created
- **FR-SPLIT-009:** System shall track settlement status for each participant
- **FR-SPLIT-010:** Users shall be able to mark participants as settled/unsettled

#### 3.5.2 Balance Calculation
- **FR-BALANCE-001:** System shall calculate overall balances between users
- **FR-BALANCE-002:** System shall provide balance breakdown by group
- **FR-BALANCE-003:** System shall optimize debt settlements (minimize number of transactions)
- **FR-BALANCE-004:** System shall show who owes whom and how much
- **FR-BALANCE-005:** Balance calculations shall consider only unsettled expenses

#### 3.5.3 Activity Tracking
- **FR-ACTIVITY-001:** System shall log all split expense activities
- **FR-ACTIVITY-002:** Activity types include: EXPENSE_CREATED, SETTLEMENT_UPDATED, EXPENSE_DELETED
- **FR-ACTIVITY-003:** Users shall be able to view activity history with pagination
- **FR-ACTIVITY-004:** Activities shall be filterable by group

### 3.6 Reporting & Analytics

#### 3.6.1 Book Reports
- **FR-REPORT-001:** Users shall be able to generate overall book reports (total income, expense, balance)
- **FR-REPORT-002:** Users shall be able to generate date-range reports for specific books
- **FR-REPORT-003:** Users shall be able to generate PDF reports for books
- **FR-REPORT-004:** PDF reports shall include summary and detailed transaction listing

#### 3.6.2 User-Level Reports
- **FR-REPORT-005:** Users shall be able to generate overall financial reports across all books
- **FR-REPORT-006:** Users shall be able to generate user-level date-range reports
- **FR-REPORT-007:** System shall calculate current month savings
- **FR-REPORT-008:** Users shall be able to generate date-range PDF reports

#### 3.6.3 PDF Export
- **FR-PDF-001:** System shall generate downloadable PDF reports
- **FR-PDF-002:** PDF download URLs shall be temporary with expiration
- **FR-PDF-003:** PDF files shall be deleted after download for security
- **FR-PDF-004:** System shall generate unique tokens for PDF downloads

#### 3.6.4 Split Expense Reports
- **FR-SPLIT-REPORT-001:** Users shall be able to export group expense reports as PDF
- **FR-SPLIT-REPORT-002:** PDF exports shall include expense summary, participant details, and balances

---

## 4. Non-Functional Requirements

### 4.1 Security

#### 4.1.1 Authentication & Authorization
- **NFR-SEC-001:** All API endpoints except authentication endpoints shall require JWT authentication
- **NFR-SEC-002:** JWT tokens shall have configurable expiration (default 24 hours)
- **NFR-SEC-003:** Passwords/sensitive data shall not be logged
- **NFR-SEC-004:** System shall implement CORS with configurable allowed origins
- **NFR-SEC-005:** Refresh tokens shall be stored securely and revocable
- **NFR-SEC-006:** System shall invalidate old OTPs when new ones are requested
- **NFR-SEC-007:** OTP verification shall have maximum retry attempts

#### 4.1.2 Data Protection
- **NFR-SEC-008:** User data shall be isolated (users can only access their own data)
- **NFR-SEC-009:** Group members can only access group data they are part of
- **NFR-SEC-010:** Temporary PDF files shall be deleted after access
- **NFR-SEC-011:** File uploads shall have size restrictions (5MB max per file, 10MB max request)

### 4.2 Performance

- **NFR-PERF-001:** System shall support pagination for all list endpoints
- **NFR-PERF-002:** Default page size shall be 20 items
- **NFR-PERF-003:** System shall implement caching with Caffeine (max 1000 items, 5-minute expiration)
- **NFR-PERF-004:** Database queries shall use appropriate indexes
- **NFR-PERF-005:** API response time shall be under 500ms for 95% of requests
- **NFR-PERF-006:** System shall handle concurrent requests efficiently

### 4.3 Reliability & Availability

- **NFR-REL-001:** System shall have health check endpoints for monitoring
- **NFR-REL-002:** System shall implement proper error handling with meaningful error messages
- **NFR-REL-003:** Database transactions shall maintain ACID properties
- **NFR-REL-004:** System shall use Liquibase for version-controlled database migrations
- **NFR-REL-005:** Application uptime shall be 99.5% or higher

### 4.4 Scalability

- **NFR-SCALE-001:** System shall be stateless to support horizontal scaling
- **NFR-SCALE-002:** Database connection pooling shall be configured appropriately
- **NFR-SCALE-003:** System shall support deployment in containerized environments (Docker)
- **NFR-SCALE-004:** Application shall be deployable across multiple environments (dev, staging, production)

### 4.5 Maintainability

- **NFR-MAINT-001:** Code shall follow Clean Architecture principles
- **NFR-MAINT-002:** System shall have comprehensive API documentation (OpenAPI/Swagger)
- **NFR-MAINT-003:** Database schema changes shall be managed through Liquibase
- **NFR-MAINT-004:** Application configuration shall support environment-specific settings
- **NFR-MAINT-005:** Code shall follow Java coding standards and best practices
- **NFR-MAINT-006:** System shall have proper logging at appropriate levels

### 4.6 Data Integrity

- **NFR-DATA-001:** All monetary values shall use BigDecimal for precision
- **NFR-DATA-002:** Currency codes shall be validated
- **NFR-DATA-003:** Date/time shall be stored in UTC format
- **NFR-DATA-004:** Database constraints shall enforce data integrity
- **NFR-DATA-005:** Cascading deletes shall be implemented appropriately

### 4.7 Monitoring & Observability

- **NFR-MONITOR-001:** System shall expose actuator endpoints (health, info, metrics)
- **NFR-MONITOR-002:** Health checks shall show detailed component status
- **NFR-MONITOR-003:** Application logs shall be structured and meaningful
- **NFR-MONITOR-004:** System shall support integration with monitoring tools (Prometheus)

---

## 5. API Endpoints

### 5.1 Authentication APIs (`/api/auth`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/auth/send-otp` | Send OTP to mobile number | No |
| POST | `/auth/resend-otp` | Resend OTP | No |
| POST | `/auth/verify-otp` | Verify OTP and login | No |
| POST | `/auth/refresh-token` | Refresh access token | No |
| POST | `/auth/logout` | Logout from current device | Yes |
| POST | `/auth/logout-all` | Logout from all devices | Yes |
| GET | `/auth/generate-client-token` | Generate client API token | No (Dev only) |

### 5.2 User Profile APIs (`/api/users`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/users/profile` | Get user profile | Yes |
| PUT | `/users/profile` | Update user profile | Yes |
| POST | `/users/profile/upload-image` | Upload profile image | Yes |

### 5.3 Book Management APIs (`/api/books`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/books` | Create a new book | Yes |
| GET | `/books` | Get all user books (paginated) | Yes |
| GET | `/books/{bookId}` | Get book by ID | Yes |
| DELETE | `/books/{bookId}` | Delete a book | Yes |

### 5.4 Entry Management APIs (`/api/entries`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/entries` | Create a new entry | Yes |
| PUT | `/entries/{entryId}` | Update an entry | Yes |
| DELETE | `/entries/{entryId}` | Delete an entry | Yes |
| GET | `/entries/{entryId}` | Get entry by ID | Yes |
| GET | `/entries?bookId={id}` | Get entries by book (paginated) | Yes |

### 5.5 Group Management APIs (`/api/groups`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/groups` | Create a new group | Yes |
| GET | `/groups` | Get user groups (paginated) | Yes |
| GET | `/groups/{groupId}` | Get group by ID | Yes |
| PUT | `/groups/{groupId}` | Update group details | Yes |
| DELETE | `/groups/{groupId}` | Delete a group | Yes |
| GET | `/groups/{groupId}/members` | Get group members | Yes |
| POST | `/groups/{groupId}/members` | Add member to group | Yes |
| DELETE | `/groups/{groupId}/members/{userId}` | Remove member from group | Yes |
| POST | `/groups/{groupId}/leave` | Leave a group | Yes |
| PUT | `/groups/{groupId}/admin/{userId}` | Make member admin | Yes |

### 5.6 Split Expense APIs (`/api/splits`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/splits` | Create split expense | Yes |
| GET | `/splits` | Get user split expenses (paginated) | Yes |
| GET | `/splits/{expenseId}` | Get split expense by ID | Yes |
| DELETE | `/splits/{expenseId}` | Delete split expense | Yes |
| GET | `/splits/{expenseId}/participants` | Get split participants | Yes |
| PUT | `/splits/{expenseId}/participants/{userId}/settlement` | Update settlement status | Yes |
| GET | `/splits/groups/{groupId}` | Get group split expenses | Yes |

### 5.7 Balance & Settlement APIs (`/api/balances`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/balances` | Get overall user balances | Yes |
| GET | `/balances/groups/{groupId}` | Get group balances | Yes |
| POST | `/settlements` | Create settlement record | Yes |
| GET | `/settlements` | Get settlement history | Yes |

### 5.8 Activity APIs (`/api/activities`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/activities` | Get user activities (paginated) | Yes |
| GET | `/activities/groups/{groupId}` | Get group activities | Yes |

### 5.9 Report APIs (`/api/reports`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/reports/overall/{bookId}` | Get overall book report | Yes |
| GET | `/reports/date-range/{bookId}` | Get date-range book report | Yes |
| GET | `/reports/user/overall` | Get overall user report | Yes |
| GET | `/reports/user/date-range` | Get user date-range report | Yes |
| GET | `/reports/pdf-url/book/{bookId}` | Get book PDF download URL | Yes |
| GET | `/reports/pdf-url/date-range` | Get date-range PDF URL | Yes |
| GET | `/reports/download/{token}` | Download PDF report | Yes |

### 5.10 Split PDF Export APIs (`/api/splits/pdf`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/splits/pdf/groups/{groupId}` | Generate group expense PDF | Yes |
| GET | `/splits/pdf/download/{token}` | Download split expense PDF | Yes |

---

## 6. Data Models

### 6.1 Core Entities

#### User Entity
```
- id: Long (PK)
- email: String (unique, max 100)
- password: String (nullable)
- firstName: String (max 50)
- lastName: String (max 50)
- phoneNumber: String (unique, required, max 15)
- username: String (unique, max 50)
- profileImageUrl: String (max 500)
- role: UserRole (CUSTOMER, ADMIN)
- status: UserStatus (ACTIVE, INACTIVE, SUSPENDED)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

#### Book Entity
```
- id: Long (PK)
- userId: Long (FK to User)
- name: String (required, max 100)
- description: String
- currency: String (3 chars, default: INR)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

#### Entry Entity
```
- id: Long (PK)
- bookId: Long (FK to Book)
- type: EntryType (INCOME, EXPENSE)
- name: String (required, max 200)
- amount: BigDecimal (precision 15, scale 2)
- currency: String (3 chars, default: INR)
- dateTime: LocalDateTime
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

#### Group Entity
```
- id: Long (PK)
- name: String (required)
- description: String
- adminUserId: Long (FK to User)
- currency: String (3 chars, default: INR)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

#### GroupMember Entity
```
- id: Long (PK)
- groupId: Long (FK to Group)
- userId: Long (FK to User)
- isAdmin: Boolean
- joinedAt: LocalDateTime
```

#### SplitExpense Entity
```
- id: Long (PK)
- description: String
- totalAmount: BigDecimal (precision 15, scale 2)
- currency: String (3 chars, default: INR)
- paidByUserId: Long (FK to User)
- groupId: Long (FK to Group, nullable)
- splitType: SplitType (EQUAL, PERCENTAGE, EXACT)
- createdByUserId: Long (FK to User)
- createdAt: LocalDateTime
```

#### SplitParticipant Entity
```
- id: Long (PK)
- splitExpenseId: Long (FK to SplitExpense)
- userId: Long (FK to User)
- amountOwed: BigDecimal (precision 15, scale 2)
- splitValue: BigDecimal (for percentage/exact splits)
- isSettled: Boolean
- settledAt: LocalDateTime
```

#### Settlement Entity
```
- id: Long (PK)
- groupId: Long (FK to Group, nullable)
- fromUserId: Long (FK to User)
- toUserId: Long (FK to User)
- amount: BigDecimal (precision 15, scale 2)
- currency: String
- settledAt: LocalDateTime
- notes: String
```

#### SplitActivity Entity
```
- id: Long (PK)
- groupId: Long (FK to Group, nullable)
- userId: Long (FK to User)
- activityType: ActivityType (EXPENSE_CREATED, SETTLEMENT_UPDATED, EXPENSE_DELETED)
- description: String
- referenceId: Long
- createdAt: LocalDateTime
```

#### OtpRequest Entity
```
- id: Long (PK)
- mobileNumber: String
- otpCode: String
- otpType: OtpType (LOGIN, VERIFICATION)
- status: OtpStatus (PENDING, VERIFIED, EXPIRED)
- attemptsCount: Integer
- expiresAt: LocalDateTime
- createdAt: LocalDateTime
```

#### RefreshToken Entity
```
- id: Long (PK)
- userId: Long (FK to User)
- token: String (unique)
- expiresAt: LocalDateTime
- isRevoked: Boolean
- createdAt: LocalDateTime
```

---

## 7. Configuration Requirements

### 7.1 Environment Variables

#### Database Configuration
- `DB_HOST` - Database host (default: localhost)
- `DB_PORT` - Database port (default: 5432)
- `DB_NAME` - Database name (default: kash_save_db)
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password

#### Security Configuration
- `JWT_SECRET_KEY` - Secret key for JWT signing
- `JWT_EXPIRATION` - JWT token expiration in milliseconds (default: 86400000)
- `CLIENT_TOKEN` - Client API token for authentication

#### Email Configuration
- `MAIL_HOST` - Mail server host (default: smtp.gmail.com)
- `MAIL_PORT` - Mail server port (default: 587)
- `MAIL_USERNAME` - Email username
- `MAIL_PASSWORD` - Email password

#### File Upload Configuration
- `UPLOAD_DIR` - Directory for file uploads (default: uploads)
- `MAX_FILE_SIZE` - Maximum file size (default: 5MB)
- `MAX_UPLOAD_FILE_SIZE` - Maximum upload file size (default: 5MB)

#### Application Configuration
- `BASE_URL` - Application base URL (default: http://localhost:8080)
- `CORS_ALLOWED_ORIGINS` - Comma-separated allowed origins

#### Payment Configuration (Future)
- `STRIPE_PUBLIC_KEY` - Stripe public key
- `STRIPE_SECRET_KEY` - Stripe secret key

### 7.2 Application Profiles

- **dev** - Development environment
- **staging** - Staging environment
- **prod** - Production environment

---

## 8. Deployment Requirements

### 8.1 Infrastructure

- **Database:** PostgreSQL 12+ with persistent storage
- **Application Server:** Java 17 runtime environment
- **Reverse Proxy:** Nginx (for SSL termination and load balancing)
- **Container Runtime:** Docker & Docker Compose
- **Monitoring:** Prometheus for metrics collection

### 8.2 Docker Configuration

- Application runs on port 8080 (internal)
- PostgreSQL runs on port 5432
- Nginx reverse proxy on ports 80/443
- Docker Compose configurations for staging and production
- Multi-stage builds for optimized image size

### 8.3 CI/CD Pipeline

- GitHub Actions workflows for:
  - Continuous Integration (build and test)
  - Staging deployment
  - Production deployment
  - Rollback mechanism

### 8.4 Environment-Specific Settings

#### Development
- Database: Local PostgreSQL
- Port: 8080
- Log level: DEBUG
- Show SQL: true

#### Staging
- Database: Staging PostgreSQL instance
- Docker containerized deployment
- SSL enabled
- Log level: INFO

#### Production
- Database: Production PostgreSQL with backups
- Docker containerized deployment
- SSL enabled with certificates
- Log level: WARN
- Performance monitoring enabled

---

## 9. Integration Requirements

### 9.1 Third-Party Integrations

#### Email Service
- SMTP integration for sending OTPs
- Support for Gmail SMTP
- TLS/STARTTLS support

#### Payment Gateway (Future)
- Stripe integration for premium features
- Secure payment processing

### 9.2 File Storage
- Local file system storage for uploads
- Support for profile image uploads
- Temporary storage for generated PDFs

---

## 10. Testing Requirements

### 10.1 Unit Testing
- Service layer unit tests
- Domain logic unit tests
- Repository unit tests

### 10.2 Integration Testing
- API endpoint integration tests
- Database integration tests with Testcontainers
- Security integration tests

### 10.3 Test Coverage
- Minimum 70% code coverage
- Critical business logic: 90%+ coverage

---

## 11. Security Requirements

### 11.1 Authentication Flow
1. User requests OTP by providing mobile number
2. System validates mobile number and checks rate limits
3. System generates OTP (4 digits) and sends to user
4. User submits OTP for verification
5. System validates OTP and creates/retrieves user account
6. System generates JWT access token and refresh token
7. User uses access token for subsequent API calls
8. When access token expires, user refreshes using refresh token

### 11.2 Authorization Rules
- Users can only access their own data (books, entries, reports)
- Group members can access group data and split expenses
- Only group admins can modify group settings
- Only expense creators can delete split expenses

### 11.3 Security Headers
- CORS configuration with allowed origins
- Content Security Policy headers
- XSS protection headers

---

## 12. Constraints & Assumptions

### 12.1 Constraints
- OTP is currently hardcoded to "1234" for development
- Single device login (new login invalidates old refresh token)
- Maximum file upload size: 5MB
- Maximum request size: 10MB
- PDF download links expire after first use
- OTP expires after configured time period

### 12.2 Assumptions
- Users have valid mobile numbers
- Users have access to email for notifications
- Users understand basic expense tracking concepts
- Currency codes follow ISO 4217 standard
- All monetary calculations use INR as default currency

---

## 13. Future Enhancements

### 13.1 Planned Features
- Email/SMS notifications for split expenses
- Receipt image uploads for expenses
- Recurring expense support
- Budget planning and tracking
- Multi-currency support with exchange rates
- Premium subscription features
- Mobile app integration
- Social features (expense feed, comments)
- Export to Excel/CSV
- Advanced analytics and insights
- Bill splitting by scanning receipts (OCR)
- Integration with payment apps for settlements

### 13.2 Scalability Improvements
- Redis caching for improved performance
- Cloud storage for file uploads (S3, Azure Blob)
- Message queue for async operations (RabbitMQ, Kafka)
- Read replicas for database scaling
- API rate limiting
- WebSocket support for real-time updates

---

## 14. Compliance & Standards

### 14.1 Data Privacy
- User data encryption at rest (future)
- Secure password storage (if password auth is added)
- GDPR compliance considerations
- Right to data deletion

### 14.2 API Standards
- RESTful API design principles
- Consistent error response format
- Standard HTTP status codes
- Pagination standards
- API versioning strategy

### 14.3 Coding Standards
- Java naming conventions
- Clean code principles
- SOLID principles
- Design patterns (Repository, Service, Factory)
- Proper exception handling

---

## 15. Support & Maintenance

### 15.1 Monitoring
- Application health checks via actuator
- Database connection monitoring
- Performance metrics (response time, throughput)
- Error rate monitoring
- Resource utilization tracking

### 15.2 Logging
- Structured logging with proper levels
- Request/response logging
- Security event logging (login attempts, failures)
- Business event logging (expense creation, settlements)

### 15.3 Backup & Recovery
- Regular database backups
- Point-in-time recovery capability
- Disaster recovery plan
- Database migration rollback procedures

---

## 16. Documentation

### 16.1 API Documentation
- OpenAPI/Swagger UI available at `/swagger-ui.html`
- API endpoint descriptions
- Request/response examples
- Error codes and messages

### 16.2 Deployment Documentation
- README-DEPLOYMENT.md for deployment instructions
- PIPELINE-SETUP.md for CI/CD configuration
- Docker setup guides
- Environment configuration guides

### 16.3 Developer Documentation
- Code architecture overview
- Development setup guide
- Database schema documentation
- Contribution guidelines

---

## Appendix A: API Response Format

### Success Response
```json
{
  "success": true,
  "data": { ... },
  "message": "Operation successful"
}
```

### Error Response
```json
{
  "success": false,
  "error": "Error message",
  "message": "Detailed error description"
}
```

### Paginated Response
```json
{
  "success": true,
  "data": {
    "content": [ ... ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 100,
    "totalPages": 5,
    "first": true,
    "last": false
  }
}
```

---

## Appendix B: Database Schema Conventions

- Table names: lowercase with underscores (e.g., `users`, `split_expenses`)
- Primary keys: `id` (BIGINT AUTO_INCREMENT)
- Foreign keys: `{entity}_id` (e.g., `user_id`, `book_id`)
- Timestamps: `created_at`, `updated_at` (LocalDateTime)
- Enums: stored as VARCHAR
- Monetary values: DECIMAL(15,2)
- All tables have audit timestamps

---

## Document Version

**Version:** 1.0
**Date:** October 2025
**Status:** Active
**Last Updated:** Generated from codebase analysis

---

**End of Requirements Document**