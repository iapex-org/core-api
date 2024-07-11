package com.iapex.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public CacheManager cacheManager() {
        // CONFIGURA EL ADMINISTRADOR DE CACHÉ CON LOS NOMBRES DE LAS CACHÉS
        // SU PRINCIPAL FUNCIÓN ES DEFINIR LA LÓGICA EN CÓMO FUNCIONA LA LÓGICA PARA LA EXPIRACIÓN Y VERIFICACIÓN DE CACHÉS
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCacheNames(Arrays.asList("verificationCodes", "emailToCodeCache", "codeToEmailCache"));
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    // CONFIGURA LAS PROPIEDADES DE CAFFEINE
    Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES); // EXPIRA DESPUÉS DE 5 MINUTOS DE INACTIVIDAD
    }
}
