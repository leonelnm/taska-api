package com.codigozerocuatro.taska.infra.config;

import com.codigozerocuatro.taska.domain.model.CacheKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCacheNames(List.of(CacheKey.PUESTOS, CacheKey.TURNOS));
        return cacheManager;
    }

    @Bean
    public CacheManager cacheManagerUser(@Value("${app.cache.user.spec:}") String spec) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(CacheKey.USER);
        cacheManager.setCacheSpecification(spec);
        return cacheManager;
    }
}
