//package com.wordsystem.newworldbridge.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    @Bean
//    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable()) // Correct way to disable CSRF in the latest Spring versions
//                .authorizeHttpRequests(authorizeRequests ->
//                        authorizeRequests
//                                .requestMatchers("/hello").permitAll() // Allow access to the "/hello" endpoint
//                                .anyRequest().authenticated())
//                .oauth2Login(oauth2 ->
//                        oauth2.successHandler((request, response, authentication) -> {
//                            response.sendRedirect("/hello"); // Custom redirect after successful login
//                        })
//                );
//        return http.build();
//    }
//}
