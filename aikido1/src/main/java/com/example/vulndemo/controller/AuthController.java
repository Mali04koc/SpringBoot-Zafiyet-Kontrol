package com.example.vulndemo.controller;

import com.example.vulndemo.model.User;
import com.example.vulndemo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    /*
     * VULN-SAST-6: Zayif/eski hash algoritmasi kullanimi (MD5).
     * MD5 collision saldirilarina acik ve sifre hashleme icin uygun degildir
     * (hizli hesaplanabildigi icin brute-force/rainbow table saldirilarina karsi savunmasizdir).
     * FIX: org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
     * veya Argon2PasswordEncoder kullan.
     */
    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(password.getBytes());

        StringBuilder hexHash = new StringBuilder();
        for (byte b : digest) {
            hexHash.append(String.format("%02x", b));
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(hexHash.toString());
        userRepository.save(user);

        return "Kullanici olusturuldu: " + username;
    }
}
