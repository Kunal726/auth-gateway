package com.projects.marketmosaic.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "marketmosaic_admins")
@Data
public class AdminEntity {
    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "admin_notes")
    private String adminNotes;

    @Column(name = "account_verified")
    private boolean accountVerified = true;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}