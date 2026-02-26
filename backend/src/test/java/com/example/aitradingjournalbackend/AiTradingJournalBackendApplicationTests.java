package com.example.aitradingjournalbackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.ai.openai.api-key=test-key",
    "spring.datasource.url=jdbc:h2:mem:testdb-main;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "app.jwt.secret=this_is_a_test_secret_key_with_more_than_32_chars",
    "app.jwt.expiration-seconds=3600",
    "app.activation.base-url=http://localhost:8080",
    "app.activation.from-email=test@example.com",
    "app.activation.token-expiration-hours=24",
    "spring.mail.host=localhost",
    "spring.mail.port=1025",
    "spring.mail.username=",
    "spring.mail.password=",
    "spring.mail.properties.mail.smtp.auth=false",
    "spring.mail.properties.mail.smtp.starttls.enable=false"
})
class AiTradingJournalBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
