# AI Trading Journal Backend

REST API for an AI-assisted trading journal application. The backend handles:

- user registration and authentication with JWT
- transaction CRUD
- Excel transaction import
- AI analysis of recent trades
- dashboard chart data for a selected date range

## Tech Stack

- Java 25
- Spring Boot 4
- Spring Security
- Spring Data JPA
- PostgreSQL
- Spring AI

## Requirements

- JDK 25
- Docker Desktop or a local PostgreSQL instance
- environment variables configured as described below
- Frontend: <a href="https://github.com/sebadabrowski95/ai-trading-journal-frontend-react">ai-trading-journal-frontend-react</a>

## Configuration

The application uses environment variables:

```env
POSTGRES_HOST=127.0.0.1
POSTGRES_PORT=5432
POSTGRES_DB=trading_journal
POSTGRES_USER=trading_user
POSTGRES_PASSWORD=change_me

APP_JWT_SECRET=your-very-strong-secret
APP_JWT_EXPIRATION_SECONDS=86400

APP_ACTIVATION_BASE_URL=http://localhost:3000/activate
APP_ACTIVATION_FROM_EMAIL=no-reply@example.com
APP_ACTIVATION_TOKEN_EXPIRATION_HOURS=24

APP_PASSWORD_RESET_BASE_URL=http://localhost:3000/reset-password
APP_PASSWORD_RESET_FROM_EMAIL=no-reply@example.com
APP_PASSWORD_RESET_TOKEN_EXPIRATION_HOURS=2

MAIL_HOST=smtp.example.com
MAIL_PORT=587
MAIL_USERNAME=user
MAIL_PASSWORD=password
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true

SPRING_AI_GROQ_API_KEY=your-groq-api-key
```

AI is configured against the Groq OpenAI-compatible endpoint:

- `base-url`: `https://api.groq.com/openai`
- model: `llama-3.3-70b-versatile`

## Running the Project

Start PostgreSQL with Docker Compose:

```bash
docker compose up -d
```

Start the application:

```bash
./gradlew bootRun
```

The API runs by default at:

```text
http://127.0.0.1:8080
```

Run tests:

```bash
./gradlew test
```

## Authentication

After login, send the JWT token in:

```http
Authorization: Bearer <token>
```

Public endpoints:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/activate`
- `POST /api/auth/password-reset/request`
- `POST /api/auth/password-reset/confirm`

All other endpoints require authentication.

## API Endpoints

## Auth

### `POST /api/auth/register`

Creates a new account and sends an activation email if registration is possible.

Required body fields:

- `email`
- `password`

Request example:

```json
{
  "email": "user@example.com",
  "password": "StrongPassword123!"
}
```

Response example:

```json
{
  "message": "If registration is possible, we have sent an activation email."
}
```

### `POST /api/auth/activate`

Activates an account using the email activation token.

Required body fields:

- `token`

Request example:

```json
{
  "token": "activation-token-from-email"
}
```

Response example:

```json
{
  "message": "Account activated."
}
```

### `POST /api/auth/login`

Authenticates the user and returns a JWT token.

Required body fields:

- `email`
- `password`

Request example:

```json
{
  "email": "user@example.com",
  "password": "StrongPassword123!"
}
```

Response example:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.example",
  "tokenType": "Bearer",
  "expiresIn": 86400
}
```

### `POST /api/auth/password-reset/request`

Requests a password reset email.

Required body fields:

- `email`

Request example:

```json
{
  "email": "user@example.com"
}
```

Response example:

```json
{
  "message": "If an account exists, we have sent an email with password reset instructions."
}
```

### `POST /api/auth/password-reset/confirm`

Resets the password using the reset token.

Required body fields:

- `token`
- `newPassword`

Request example:

```json
{
  "token": "reset-token-from-email",
  "newPassword": "NewStrongPassword123!"
}
```

Response example:

```json
{
  "message": "Password has been changed."
}
```

### `GET /api/auth/me`

Returns the currently authenticated user.


Response example:

```json
{
  "username": "user@example.com"
}
```

## Transactions

### `GET /api/transactions/list`

Returns all transactions for the authenticated user.

Response example:

```json
[
  {
    "id": 1,
    "position": "123456789",
    "symbol": "EURUSD",
    "type": "buy",
    "volume": 0.10,
    "openTime": "2026-04-21T08:00:00Z",
    "openPrice": 1.08123,
    "closeTime": "2026-04-21T10:15:00Z",
    "closePrice": 1.08345,
    "sl": 1.079,
    "tp": 1.085,
    "margin": 100.00,
    "commission": -1.20,
    "swap": 0.00,
    "rollover": 0.00,
    "grossPl": 22.50,
    "comment": "Breakout setup"
  }
]
```

### `GET /api/transactions/calendar?year=2026&month=4`

Returns calendar aggregates for a month.

Required query parameters:

- `year`
- `month`

Response example:

```json
{
  "year": 2026,
  "month": 4,
  "days": [
    {
      "date": "2026-04-21",
      "grossPl": 22.50,
      "tradeCount": 1
    }
  ],
  "weeks": [
    {
      "weekStart": "2026-04-20",
      "weekEnd": "2026-04-26",
      "grossPl": 22.50,
      "tradeCount": 1
    }
  ],
  "monthSummary": {
    "grossPl": 22.50,
    "tradeCount": 1
  }
}
```

### `GET /api/transactions/day?date=2026-04-21`

Returns all transactions closed on a specific day.

Required query parameters:

- `date`

Response example:

```json
{
  "date": "2026-04-21",
  "grossPl": 22.50,
  "tradeCount": 1,
  "transactions": [
    {
      "id": 1,
      "position": "123456789",
      "symbol": "EURUSD",
      "type": "buy",
      "volume": 0.10,
      "openTime": "2026-04-21T08:00:00Z",
      "openPrice": 1.08123,
      "closeTime": "2026-04-21T10:15:00Z",
      "closePrice": 1.08345,
      "sl": 1.079,
      "tp": 1.085,
      "margin": 100.00,
      "commission": -1.20,
      "swap": 0.00,
      "rollover": 0.00,
      "grossPl": 22.50,
      "comment": "Breakout setup"
    }
  ]
}
```

### `GET /api/transactions/{id}`

Returns a single transaction by ID.

Response example:

```json
{
  "id": 1,
  "position": "123456789",
  "symbol": "EURUSD",
  "type": "buy",
  "volume": 0.10,
  "openTime": "2026-04-21T08:00:00Z",
  "openPrice": 1.08123,
  "closeTime": "2026-04-21T10:15:00Z",
  "closePrice": 1.08345,
  "sl": 1.079,
  "tp": 1.085,
  "margin": 100.00,
  "commission": -1.20,
  "swap": 0.00,
  "rollover": 0.00,
  "grossPl": 22.50,
  "comment": "Breakout setup"
}
```

### `POST /api/transactions`

Creates a transaction.

Required body fields:

- `position`
- `symbol`
- `type`
- `volume`
- `openTime`
- `openPrice`
- `grossPl`

Optional body fields:

- `closeTime`
- `closePrice`
- `sl`
- `tp`
- `margin`
- `commission`
- `swap`
- `rollover`
- `comment`

Request example:

```json
{
  "position": "123456789",
  "symbol": "EURUSD",
  "type": "buy",
  "volume": 0.10,
  "openTime": "2026-04-21T08:00:00Z",
  "openPrice": 1.08123,
  "closeTime": "2026-04-21T10:15:00Z",
  "closePrice": 1.08345,
  "sl": 1.07900,
  "tp": 1.08500,
  "margin": 100.00,
  "commission": -1.20,
  "swap": 0.00,
  "rollover": 0.00,
  "grossPl": 22.50,
  "comment": "Breakout setup"
}
```

Response example:

```json
{
  "id": 1,
  "position": "123456789",
  "symbol": "EURUSD",
  "type": "buy",
  "volume": 0.10,
  "openTime": "2026-04-21T08:00:00Z",
  "openPrice": 1.08123,
  "closeTime": "2026-04-21T10:15:00Z",
  "closePrice": 1.08345,
  "sl": 1.079,
  "tp": 1.085,
  "margin": 100.00,
  "commission": -1.20,
  "swap": 0.00,
  "rollover": 0.00,
  "grossPl": 22.50,
  "comment": "Breakout setup"
}
```

### `PUT /api/transactions/{id}`

Updates an existing transaction.

Required body fields:

- `position`
- `symbol`
- `type`
- `volume`
- `openTime`
- `openPrice`
- `grossPl`

Optional body fields:

- `closeTime`
- `closePrice`
- `sl`
- `tp`
- `margin`
- `commission`
- `swap`
- `rollover`
- `comment`

Request example:

```json
{
  "position": "123456789",
  "symbol": "EURUSD",
  "type": "sell",
  "volume": 0.20,
  "openTime": "2026-04-21T08:00:00Z",
  "openPrice": 1.08123,
  "closeTime": "2026-04-21T11:00:00Z",
  "closePrice": 1.07880,
  "sl": 1.08300,
  "tp": 1.07800,
  "margin": 120.00,
  "commission": -1.50,
  "swap": 0.00,
  "rollover": 0.00,
  "grossPl": 48.60,
  "comment": "Updated after review"
}
```

Response example:

```json
{
  "id": 1,
  "position": "123456789",
  "symbol": "EURUSD",
  "type": "sell",
  "volume": 0.20,
  "openTime": "2026-04-21T08:00:00Z",
  "openPrice": 1.08123,
  "closeTime": "2026-04-21T11:00:00Z",
  "closePrice": 1.07880,
  "sl": 1.083,
  "tp": 1.078,
  "margin": 120.00,
  "commission": -1.50,
  "swap": 0.00,
  "rollover": 0.00,
  "grossPl": 48.60,
  "comment": "Updated after review"
}
```

### `POST /api/transactions/import`

Imports transactions from an Excel file.

Required body fields:

- multipart form-data field `file`

Response example:

```json
{
  "importedCount": 120,
  "added": 90,
  "updated": 30
}
```

### `DELETE /api/transactions/{id}`

Deletes a transaction.



## AI

### `POST /api/ai/chat`

Analyzes the last `70` closed transactions of the authenticated user.


Optional body fields:

- `prompt`

Request example:

```json
{
  "prompt": "Analyze my mistakes and point out overtrading."
}
```

Response example:

```json
{
  "message": "Your recent trades show a positive expectancy, but there are signs of overtrading after losing positions..."
}
```

## Dashboard

### `POST /api/dashboard/charts`

Returns chart-ready transactions and the unique symbol list for the selected date range.

Required body fields:

- `dateFrom`
- `dateTo`

Request example:

```json
{
  "dateFrom": "2026-04-01",
  "dateTo": "2026-04-21"
}
```

Response example:

```json
{
  "transactions": [
    {
      "closeTime": "2026-04-03T09:21:00Z",
      "symbol": "EURUSD",
      "type": "buy",
      "grossPl": 15.20
    },
    {
      "closeTime": "2026-04-03T14:45:00Z",
      "symbol": "XAUUSD",
      "type": "sell",
      "grossPl": -8.50
    }
  ],
  "symbols": [
    "EURUSD",
    "XAUUSD"
  ]
}
```

## Project Structure

```text
src/main/java/com/example/aitradingjournalbackend
|-- AI
|-- auth
|-- config
|-- dashboard
|-- transaction
`-- user
```

## Notes

- timestamps are handled in UTC
- Hibernate is configured with `spring.jpa.hibernate.ddl-auto=update`
- the AI endpoint analyzes only closed transactions
