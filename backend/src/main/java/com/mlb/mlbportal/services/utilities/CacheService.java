package com.mlb.mlbportal.services.utilities;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CacheService {
    private final CacheManager cacheManager;

    public void clearCaches(String... cacheNames) {
        for (String name : cacheNames) {
            Cache cache = this.cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        }
    }
}