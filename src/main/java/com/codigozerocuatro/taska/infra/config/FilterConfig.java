package com.codigozerocuatro.taska.infra.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@RequiredArgsConstructor
@Configuration
public class FilterConfig {

    private final HttpLoggingFilter httpLoggingFilter;

    @Bean
    public FilterRegistrationBean<HttpLoggingFilter> loggingFilter() {
        FilterRegistrationBean<HttpLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        
        registrationBean.setFilter(httpLoggingFilter);
        registrationBean.addUrlPatterns("/api/*"); // Solo aplicar a rutas de API
        registrationBean.setName("httpLoggingFilter");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE); // Ejecutar primero
        
        return registrationBean;
    }
}
