package com.mlb.mlbportal.services.utilities;

import com.mlb.mlbportal.handler.notFound.CacheNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CacheService {
    private final CacheManager cacheManager;

    public List<String> getCaches() {
        return this.cacheManager.getCacheNames().stream().toList();
    }

    public void clearSingleCache(String cacheName) {
        Cache cache = this.cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new CacheNotFoundException(cacheName);
        }
        cache.clear();
    }

    public void clearCaches(String... cacheNames) {
        for (String name : cacheNames) {
            Cache cache = this.cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        }
    }

    public void clearAllCaches() {
        for (String cache : this.cacheManager.getCacheNames()) {
            this.clearSingleCache(cache);
        }
    }
}