package com.re.ss17;

import static org.assertj.core.api.Assertions.assertThat;

import com.re.ss17.config.CacheConfig;
import com.re.ss17.service.FlashSaleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

@DisplayName("BT04 - chong Cache Stampede: sync = true tren cache 'flash-sale', key #id")
class FlashSaleStampedeTest {

    @Test
    void cacheableDungSyncTrueVaMotCache() throws Exception {
        Cacheable cacheable = FlashSaleService.class.getMethod("getProduct", Long.class)
                .getAnnotation(Cacheable.class);

        assertThat(cacheable).isNotNull();
        assertThat(cacheable.sync()).isTrue();
        assertThat(cacheable.cacheNames()).containsExactly("flash-sale");
        assertThat(cacheable.key()).isEqualTo("#id");
        assertThat(cacheable.unless()).isEmpty();
        assertThat(CacheConfig.FLASH_SALE_CACHE).isEqualTo("flash-sale");
    }

    @Test
    void getProductKhongDungCachePut() throws Exception {
        assertThat(FlashSaleService.class.getMethod("getProduct", Long.class)
                .getAnnotation(CachePut.class)).isNull();
    }
}
