package com.re.ss17.service;

import com.re.ss17.config.CacheConfig;
import com.re.ss17.dto.FlashSaleProductDTO;
import com.re.ss17.repository.FlashSaleProductRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class FlashSaleService {

    private static final Logger log = LoggerFactory.getLogger(FlashSaleService.class);

    private final FlashSaleProductRepository flashSaleProductRepository;
    private final long queryDelayMillis;

    public FlashSaleService(FlashSaleProductRepository flashSaleProductRepository,
                            @Value("${flash-sale.db.query-delay:2000ms}") long queryDelayMillis) {
        this.flashSaleProductRepository = flashSaleProductRepository;
        this.queryDelayMillis = queryDelayMillis;
    }

    @Cacheable(cacheNames = CacheConfig.FLASH_SALE_CACHE, key = "#id", sync = true)
    public List<FlashSaleProductDTO> getProduct(Long id) {
        log.info("Fetching from Database for product {}", id);
        simulateSlowQuery();
        List<FlashSaleProductDTO> products = flashSaleProductRepository.findById(id)
                .map(product -> List.of(FlashSaleProductDTO.from(product)))
                .orElseGet(List::of);
        log.info("Fetching from Database for product {} done, {} ban ghi", id, products.size());
        return products;
    }

    public long getQueryDelayMillis() {
        return queryDelayMillis;
    }

    private void simulateSlowQuery() {
        try {
            Thread.sleep(queryDelayMillis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Bi ngat khi gia lap truy van DB", ex);
        }
    }
}
