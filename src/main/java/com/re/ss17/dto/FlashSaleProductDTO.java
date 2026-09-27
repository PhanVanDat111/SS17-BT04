package com.re.ss17.dto;

import com.re.ss17.model.FlashSaleProduct;

public record FlashSaleProductDTO(Long id, String name, Long price, Integer stock) {

    public static FlashSaleProductDTO from(FlashSaleProduct product) {
        return new FlashSaleProductDTO(product.id(), product.name(), product.price(), product.stock());
    }
}
