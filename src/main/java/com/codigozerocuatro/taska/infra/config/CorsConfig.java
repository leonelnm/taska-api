package com.codigozerocuatro.taska.infra.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final AppProperties appProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Aplica la configuración a todas las rutas que comiencen con /api
                .allowedOrigins(appProperties.cors().allowedOrigins().toArray(String[]::new)) // Permite peticiones desde el dominio
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Permite los métodos HTTP que necesitas
                .allowedHeaders("*") // Permite todos los headers
                .allowCredentials(true); // Permite el envío de cookies o credenciales de autenticación si las tuvieras
    }
}