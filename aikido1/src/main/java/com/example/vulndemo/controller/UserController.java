package com.example.vulndemo.controller;

import com.example.vulndemo.model.User;
import com.example.vulndemo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/*
 * VULN-SAST-2: Asiri izinli CORS yapilandirmasi.
 * "*" ile her origin'e izin verip ustune allowCredentials=true demek,
 * tarayicilarin bunu reddetmesine ragmen yine de kotu bir pratiktir ve
 * coğu SAST/SCA araci tarafindan "overly permissive CORS" olarak isaretlenir.
 * FIX: allowedOrigins icine sadece bilinen, guvenilir domain(ler)i yaz.
 */
@CrossOrigin(origins = "*", allowCredentials = "true")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    // Demo amacli, gercek dosyalarin tutulacagi sabit dizin.
    private static final String UPLOAD_DIR = "/tmp/vuln-demo-uploads";

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /*
     * VULN-SAST-3: SQL Injection.
     * Kullanicidan gelen "name" parametresi dogrudan SQL string'ine
     * concat edilerek calistiriliyor. ?name=' OR '1'='1 gibi bir input
     * tum tabloyu dondurur, ya da UNION tabanli saldirilarla veri sizdirilabilir.
     * FIX: JdbcTemplate.query(sql, new Object[]{name}, rowMapper) gibi
     * parametreli (PreparedStatement) sorgu kullan, ya da dogrudan
     * Spring Data JPA query method'u (findByUsernameContaining gibi) tercih et.
     */
    @GetMapping("/search")
    public List<Map<String, Object>> searchUsers(@RequestParam String name) {
        String sql = "SELECT id, username, email FROM user WHERE username = '" + name + "'";
        return jdbcTemplate.queryForList(sql);
    }

    /*
     * VULN-SAST-4: Path Traversal.
     * "filename" parametresi dogrudan dosya sistemi yoluna eklenip okunuyor.
     * ?filename=../../../../etc/passwd gibi bir input ile sunucudaki
     * yetkisiz dosyalar okunabilir.
     * FIX: dosya adini normalize et (Path.normalize) ve sonucun hala
     * UPLOAD_DIR icinde oldugunu dogrula (whitelist / startsWith kontrolu),
     * ya da kullaniciya sadece onceden tanimli bir dosya id'si uzerinden erisim ver.
     */
    @GetMapping("/file")
    public String readUserFile(@RequestParam String filename) throws IOException {
        Path basePath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        Path targetPath=basePath.resolve(filename).normalize();

        if(!targetPath.startsWith(basePath)){
            throw new SecurityException("Güvenlik İhlali: Dizin dışına çıkamazsın")
        }
    }

    /*
     * VULN-SAST-5: OS Command Injection.
     * "host" parametresi dogrudan shell komutuna eklenip calistiriliyor.
     * ?host=8.8.8.8;rm -rf / gibi bir input ile sunucu uzerinde
     * istenilen komut calistirilabilir.
     * FIX: kullanici girdisini hic bir zaman Runtime.exec/ProcessBuilder
     * icine ham haliyle verme. Gerekiyorsa girdiyi siki bir whitelist/regex
     * (sadece IPv4 formatina izin ver) ile dogrula ve ProcessBuilder'i
     * argumanlari ayri ayri liste olarak ver (shell=false), asla string concat etme.
     */
    @PostMapping("/ping")
    public String pingHost(@RequestParam String host) throws IOException {
        Process process = Runtime.getRuntime().exec("ping -c 1 " + host);
        return "Ping komutu calistirildi: " + host + " (exit kontrolu yapilmadi)";
    }
}