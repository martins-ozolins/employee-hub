package com.employeehub.employeehub.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@NoArgsConstructor
@Getter
@Setter
public class RefreshToken {

    @Id
    @GeneratedValue
    UUID id;

    @Column(name = "jti", nullable = false, unique = true)
    UUID jti;

    @Column(name = "expires_at", nullable = false)
    Instant expiresAt;

    @Column(nullable = false)
    Boolean revoked = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Instant createdAt;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User owner;

}