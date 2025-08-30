package com.codigozerocuatro.taska.infra.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Aplica la configuración a todas las rutas que comiencen con /api
                .allowedOrigins("http://localhost:5173") // Permite peticiones desde el dominio de tu frontend de SvelteKit
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Permite los métodos HTTP que necesitas
                .allowedHeaders("*") // Permite todos los headers
                .allowCredentials(true); // Permite el envío de cookies o credenciales de autenticación si las tuvieras
    }
}