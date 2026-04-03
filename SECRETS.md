# GitHub Secrets

This project uses the following GitHub Actions secrets:
- `SPRING_AI_OPENAI_API_KEY`
- `APP_JWT_SECRET`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `POSTGRES_DB`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `APP_ACTIVATION_FROM_EMAIL`
- `APP_PASSWORD_RESET_FROM_EMAIL`

This project also requires the following CI variables:
- `POSTGRES_HOST`
- `POSTGRES_PORT`
- `APP_JWT_EXPIRATION_SECONDS`
- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_SMTP_AUTH`
- `MAIL_SMTP_STARTTLS`
- `APP_ACTIVATION_BASE_URL`
- `APP_PASSWORD_RESET_BASE_URL`
- `APP_ACTIVATION_TOKEN_EXPIRATION_HOURS`
- `APP_PASSWORD_RESET_TOKEN_EXPIRATION_HOURS`

References:
- CI workflow: `/.github/workflows/build.yml`
- Application config: `/src/main/resources/application.properties`
- GitHub Secrets docs: https://docs.github.com/en/actions/security-for-github-actions/security-guides/using-secrets-in-github-actions


Local `.env` example:

```env
SPRING_AI_OPENAI_API_KEY=
APP_JWT_SECRET=
APP_JWT_EXPIRATION_SECONDS=3600

POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=
POSTGRES_USER=
POSTGRES_PASSWORD=

APP_ACTIVATION_BASE_URL=http://localhost:8080
APP_ACTIVATION_TOKEN_EXPIRATION_HOURS=24
APP_PASSWORD_RESET_BASE_URL=http://localhost:8080
APP_PASSWORD_RESET_TOKEN_EXPIRATION_HOURS=24

MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true

APP_ACTIVATION_FROM_EMAIL=your-email@gmail.com
APP_PASSWORD_RESET_FROM_EMAIL=your-email@gmail.com
```
