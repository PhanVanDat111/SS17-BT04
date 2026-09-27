package com.re.ss17.controller;

import com.re.ss17.dto.FlashSaleProductDTO;
import com.re.ss17.service.FlashSaleService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/flash-sale")
public class FlashSaleController {

    private final FlashSaleService flashSaleService;

    public FlashSaleController(FlashSaleService flashSaleService) {
        this.flashSaleService = flashSaleService;
    }

    @GetMapping("/products/{id}")
    public List<FlashSaleProductDTO> getProduct(@PathVariable Long id) {
        return flashSaleService.getProduct(id);
    }
}
