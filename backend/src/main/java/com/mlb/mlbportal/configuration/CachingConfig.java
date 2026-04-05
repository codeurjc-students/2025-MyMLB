package com.mlb.mlbportal.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CachingConfig {
    @Bean("caffeine")
    @Primary
    public CacheManager cacheConfig() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
                this.buildCache("get-players", 24 * 60),
                this.buildCache("all-stats-player-rankings", 24 * 60),
                this.buildCache("single-stat-player-rankings", 24 * 60)
        ));
        return cacheManager;
    }

    private CaffeineCache buildCache(String name, int minutesToExpire) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .initialCapacity(5_000)
                .maximumSize(20_000)
                .expireAfterWrite(minutesToExpire, TimeUnit.MINUTES)
                .recordStats()
                .build());
    }
}