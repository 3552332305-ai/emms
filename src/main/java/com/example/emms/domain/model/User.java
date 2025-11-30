package com.example.emms.domain.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String phone;
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    private Long supplierId;
    public Long getSupplierId() {
        return this.supplierId;
    }

    public enum UserRole {
        ADMIN,       // 管理员
        USER,        // 普通用户
        SUPPLIER     // 供应商用户
    }
}
