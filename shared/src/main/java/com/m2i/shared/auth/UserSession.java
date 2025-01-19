package com.m2i.shared.auth;

import com.m2i.shared.entities.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;

@Entity
@Table(name = "user_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSession implements Serializable {
    @Id
    private String sessionId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private long expirationTime;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "last_accessed")
    private long lastAccessedTime;

    @Column(name = "creation_time")
    private long creationTime;

    @Column(name = "ip_address")
    private String ipAddress;
}