package com.example.aitradingjournalbackend.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "app_users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @Setter
    private String passwordHash;

    @Column(nullable = false)
    @Setter
    private boolean enabled;

    @Column(nullable = false, columnDefinition = "integer not null default 0")
    @Setter
    private int tokenVersion;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public AppUser(String email, String passwordHash, boolean enabled) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
        this.tokenVersion = 0;
        this.createdAt = Instant.now();
    }
}
