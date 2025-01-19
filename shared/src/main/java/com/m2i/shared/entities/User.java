package com.m2i.shared.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Struct;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    @Column(unique = true)
    private String email;
    private String address;
    @Column(unique = true)
    private String cni;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;
}