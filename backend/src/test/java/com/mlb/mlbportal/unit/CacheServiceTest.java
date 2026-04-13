package com.mlb.mlbportal.unit;

import com.mlb.mlbportal.handler.notFound.CacheNotFoundException;
import com.mlb.mlbportal.services.utilities.CacheService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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
    @DisplayName("Should return a list of all cache names")
    void testGetCaches() {
        Collection<String> cacheNames = List.of("users", "standings", "rankings");
        when(this.cacheManager.getCacheNames()).thenReturn(cacheNames);

        List<String> result = this.cacheService.getCaches();

        assertThat(result.size()).isEqualTo(3);
        assertThat(result.contains("users")).isTrue();
        verify(this.cacheManager, times(1)).getCacheNames();
    }

    @Test
    @DisplayName("Should clear a single cache")
    void testClearSingleCacheSuccess() {
        Cache mockCache = mock(Cache.class);
        when(this.cacheManager.getCache("test-cache")).thenReturn(mockCache);

        this.cacheService.clearSingleCache("test-cache");

        verify(mockCache, times(1)).clear();
        verify(this.cacheManager, times(1)).getCache("test-cache");
    }

    @Test
    @DisplayName("Should throw CacheNotFoundException when a cache does not exists")
    void testClearSingleCacheNotFound() {
        when(this.cacheManager.getCache("unknown-cache")).thenReturn(null);

        assertThatThrownBy(() -> this.cacheService.clearSingleCache("unknown-cache"))
                .isInstanceOf(CacheNotFoundException.class)
                .hasMessage("Cache unknown-cache Not Found");

        verify(this.cacheManager, times(1)).getCache("unknown-cache");
    }

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

    @Test
    @DisplayName("Should clear every cache")
    void testClearAllCaches() {
        Collection<String> cacheNames = List.of("cache1", "cache2");
        Cache mockCache1 = mock(Cache.class);
        Cache mockCache2 = mock(Cache.class);

        when(this.cacheManager.getCacheNames()).thenReturn(cacheNames);
        when(this.cacheManager.getCache("cache1")).thenReturn(mockCache1);
        when(this.cacheManager.getCache("cache2")).thenReturn(mockCache2);

        this.cacheService.clearAllCaches();

        verify(mockCache1, times(1)).clear();
        verify(mockCache2, times(1)).clear();
        verify(this.cacheManager, times(1)).getCacheNames();
    }
}