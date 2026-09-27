package com.re.ss17.loadtest;

import com.re.ss17.service.FlashSaleService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "flash-sale.load-test.enabled", havingValue = "true")
public class StampedeLoadTestRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StampedeLoadTestRunner.class);

    private static final Long PRODUCT_ID = 1L;

    private final FlashSaleService flashSaleService;
    private final int threads;

    public StampedeLoadTestRunner(FlashSaleService flashSaleService,
                                  @Value("${flash-sale.load-test.threads:50}") int threads) {
        this.flashSaleService = flashSaleService;
        this.threads = threads;
    }

    @Override
    public void run(ApplicationArguments args) {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Integer>> futures = new ArrayList<>();
        long begin = System.currentTimeMillis();
        try {
            for (int i = 0; i < threads; i++) {
                futures.add(pool.submit(() -> {
                    start.await();
                    return flashSaleService.getProduct(PRODUCT_ID).size();
                }));
            }
            start.countDown();
            int succeeded = 0;
            for (Future<Integer> future : futures) {
                future.get();
                succeeded++;
            }
            log.info(">>> LOAD TEST: {}/{} request thanh cong trong {}ms (gia lap truy van DB {}ms)",
                    succeeded, threads, System.currentTimeMillis() - begin,
                    flashSaleService.getQueryDelayMillis());
        } catch (Exception ex) {
            log.error(">>> LOAD TEST that bai", ex);
        } finally {
            pool.shutdownNow();
        }
    }
}
