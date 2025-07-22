package com.pm.auth_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        // Configure HTTP security,
        // authorize -> ...: This is a lambda expression, a concise way to define the authorization rules.
        // The authorize object allows you to specify which requests are permitted.
        // authorize.anyRequest(): This means "apply the following rule to any incoming HTTP request."
        // .permitAll(): This is the rule itself. It means that anyRequest() is permitted to all users,
        // without requiring any authentication or authorization.
        // .csrf(AbstractHttpConfigurer::disable); This disables CSRF (Cross-Site Request Forgery) protection
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
        // After all the security configurations are applied, this line builds the SecurityFilterChain object,
        // which Spring Security will then use.
    }
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
        //This line instantiates BCryptPasswordEncoder. When you later deal with user registration or authentication,
        // Spring Security (or your custom UserDetailsService) will automatically discover and use this PasswordEncoder
        // bean to hash passwords before storing them in the database and to compare submitted passwords during login.
    }
}
