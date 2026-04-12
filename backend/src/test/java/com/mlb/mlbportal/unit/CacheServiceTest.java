package com.mlb.mlbportal.unit;

import com.mlb.mlbportal.services.utilities.CacheService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private CacheService cacheService;

    @Test
    @DisplayName("Should clear existing caches")
    void testClearCaches() {
        String existingCache = "get-teams";
        String nonExistingCache = "ghost-cache";

        Cache mockCache = mock(Cache.class);

        when(this.cacheManager.getCache(existingCache)).thenReturn(mockCache);
        when(this.cacheManager.getCache(nonExistingCache)).thenReturn(null);

        this.cacheService.clearCaches(existingCache, nonExistingCache);

        verify(mockCache, times(1)).clear();
        verify(this.cacheManager, times(1)).getCache(existingCache);
        verify(this.cacheManager, times(1)).getCache(nonExistingCache);
    }

    @Test
    @DisplayName("Should do nothing when no cache names are provided")
    void testClearCachesEmpty() {
        this.cacheService.clearCaches();
        verifyNoInteractions(cacheManager);
    }
}