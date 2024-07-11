package com.iapex.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class CacheCleaner implements CommandLineRunner {

    @Autowired
    private CacheManager cacheManager;

    @Override
    public void run(String... args) throws Exception {
        // LIMPIA TODAS LAS CACHÉS DISPONIBLES
        cacheManager.getCacheNames().forEach(cacheName -> {
            cacheManager.getCache(cacheName).clear();
            System.out.println("Cache cleared: " + cacheName);
        });
    }
}
