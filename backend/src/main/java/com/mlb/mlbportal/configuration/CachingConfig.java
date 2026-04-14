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
                // 1 Day Cache
                this.buildCache("get-users", 24 * 60),
                this.buildCache("get-profile", 24 * 60),
                this.buildCache("get-fav-teams", 24 * 60),
                this.buildCache("get-purchase-tickets", 24 * 60),
                this.buildCache("get-home-matches", 24 * 60),
                this.buildCache("get-away-matches", 24 * 60),
                this.buildCache("get-players", 24 * 60),
                this.buildCache("all-stats-player-rankings", 24 * 60),
                this.buildCache("single-stat-player-rankings", 24 * 60),
                this.buildCache("get-teams", 24 * 60),
                this.buildCache("get-standings", 24 * 60),
                this.buildCache("runs-per-rival", 24 * 60),
                this.buildCache("wins-per-rivals", 24 * 60),
                this.buildCache("win-distribution", 24 * 60),
                this.buildCache("historic-ranking", 24 * 60),

                // 1 Week Cache
                this.buildCache("get-rivals", 168 * 60),
                this.buildCache("get-stadiums", 168 * 60),
                this.buildCache("get-available-stadiums", 168 * 60),
                this.buildCache("search-stadium", 168 * 60),
                this.buildCache("search-team", 168 * 60),
                this.buildCache("search-player", 168 * 60)
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