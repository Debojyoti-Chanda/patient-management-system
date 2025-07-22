package com.pm.auth_service.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key secretKey; //Stores the cryptographic key used for signing JWTs

    public JwtUtil(@Value("${jwt.secret}") String  secret){
        // To safely store binary data (like a cryptographic key) in a text-based format, it's common practice to
        // Base64 encode it. Base64 converts any binary data into an ASCII string consisting of A-Z, a-z, 0-9, +, /, and =.
        // This string is safe to put into YAML or properties files.
        // jwt.secret is Base64 encoded string
        // secret.getBytes(StandardCharsets.UTF_8) converts this Base64 string into a byte array representing the characters of the Base64 string.
        byte[] keyBytes = Base64.getDecoder().decode(secret.getBytes(StandardCharsets.UTF_8));
        // The Keys.hmacShaKeyFor(byte[] secretKeyBytes) method from JJWT expects the raw, original binary bytes of
        // your secret key. It doesn't expect a Base64-encoded string that has simply been converted to bytes.
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email, String role){
         return Jwts.builder()
                 .subject(email) // Set the subject claim (email)
                 .claim("role",role) // Add a custom 'role' claim
                 .issuedAt(new Date()) // Set the issued-at time (current time)
                 .expiration(new Date(System.currentTimeMillis() + 1000L *60*60*10)) // Set expiration (10 hours from now)
                 .signWith(secretKey) // Sign the token with the secret key
                 .compact(); // Compact the token into its final String form

    }
}
