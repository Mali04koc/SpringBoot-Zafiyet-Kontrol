package com.example.vulndemo.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    // VULN-SAST-1: Sifre veritabaninda duz metin / zayif hash olarak saklaniyor.
    // FIX: BCryptPasswordEncoder (veya Argon2) ile hashleyip sakla, hic bir zaman
    // duz metin sifreyi DB'ye yazma.
    private String password;

    private String email;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}