package com.re.ss17.repository;

import com.re.ss17.model.FlashSaleProduct;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class FlashSaleProductRepository {

    private final Map<Long, FlashSaleProduct> rows = Map.of(
            1L, new FlashSaleProduct(1L, "iPhone 15 Flash Sale", 15_990_000L, 100),
            2L, new FlashSaleProduct(2L, "AirPods Pro", 4_990_000L, 250));

    public Optional<FlashSaleProduct> findById(Long id) {
        return Optional.ofNullable(rows.get(id));
    }
}
