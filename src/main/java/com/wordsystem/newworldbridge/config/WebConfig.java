package com.wordsystem.newworldbridge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:5173") // 프론트엔드 주소
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .exposedHeaders("Cross-Origin-Opener-Policy", "Cross-Origin-Embedder-Policy")
                        .allowCredentials(true);
            }
        };
    }
}

