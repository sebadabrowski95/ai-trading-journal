package com.example.aitradingjournalbackend.auth;

import com.example.aitradingjournalbackend.user.AppUser;
import com.example.aitradingjournalbackend.user.EmailVerificationToken;
import com.example.aitradingjournalbackend.user.repo.AppUserRepository;
import com.example.aitradingjournalbackend.user.repo.EmailVerificationTokenRepository;
import com.jayway.jsonpath.JsonPath;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.ai.openai.api-key=test-key",
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
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
class AuthFlowTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        tokenRepository.deleteAll();
        userRepository.deleteAll();
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .apply(springSecurity())
            .build();
    }

    @Test
    void inactiveAccountCannotLoginButCanAfterActivation() throws Exception {
        AppUser user = userRepository.save(new AppUser(
            "test-user@example.com",
            passwordEncoder.encode("test-password"),
            false
        ));
        String activationToken = UUID.randomUUID().toString();
        tokenRepository.save(new EmailVerificationToken(
            activationToken,
            user,
            Instant.now().plusSeconds(3600)
        ));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test-user@example.com\",\"password\":\"test-password\"}"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/auth/activate")
                .param("token", activationToken))
            .andExpect(status().isOk());

        String responseBody = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test-user@example.com\",\"password\":\"test-password\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isString())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresIn").value(3600))
            .andReturn()
            .getResponse()
            .getContentAsString();

        String token = JsonPath.read(responseBody, "$.accessToken");

        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("test-user@example.com"));
    }

    @Test
    void meWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized());
    }
}
