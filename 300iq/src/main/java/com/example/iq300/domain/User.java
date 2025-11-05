package com.example.iq300.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter; // 👈 (필수)
import lombok.Setter; // 👈 (필수)

@Getter // 👈 (필수)
@Setter // 👈 (필수)
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname; // 👈 (추가) 이 필드가 없었습니다.

    private String password;

    private boolean isVerified; // 👈 (추가) 이 필드가 없었습니다.
}