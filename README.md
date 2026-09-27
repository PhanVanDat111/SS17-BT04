BÀI TẬP THỰC HÀNH 4

Chống Cache Stampede bằng `sync = true`.

a) Hiện thực

- `FlashSaleService.getProduct(Long id)`:
  ```java
  @Cacheable(cacheNames = "flash-sale", key = "#id", sync = true)
  public List<FlashSaleProductDTO> getProduct(Long id) { ... }
  ```
  Thân method log đúng chuỗi `Fetching from Database for product {}` rồi `Thread.sleep(flash-sale.db.query-delay)`
  (mặc định 2000ms) và đọc repository.
- `CacheConfig` dùng `RedisCacheWriter.lockingRedisCacheWriter(...)`, không phải `nonLockingRedisCacheWriter`.
  Đây là điều kiện đủ thứ hai: `sync = true` khiến `CacheInterceptor` gọi `cache.get(key, valueLoader)`.
  Với writer khoá, `DefaultRedisCacheWriter.get(name, key, loader, ttl, ...)` kiểm tra khoá Redis `name~lock`,
  đọc lại Redis (double-check) và ghi bằng `putIfAbsent`; luồng khác thấy khoá thì `Thread.sleep` rồi kiểm tra lại.
  Writer không khoá bỏ qua toàn bộ bước này (`isLockingCacheWriter()` trả false), nên cấu hình vẫn trông đúng
  nhưng không có gì để chứng minh.
- Spring ràng buộc `sync = true`: chỉ một cache, không `unless`, không kết hợp cache operation khác. Để `unless` lại sẽ
  là `IllegalStateException` lúc khởi động chứ không phải cảnh báo âm thầm, nên annotation ở đây không có `unless`.
- Giá trị cache là `List<FlashSaleProductDTO>`; serializer là `JacksonJsonRedisSerializer` dựng trên
  `TypeFactory.createDefaultInstance().constructCollectionType(List.class, FlashSaleProductDTO.class)`. DTO có tên
  trùng với một record trong package `config` sẽ khiến serializer generic không đọc lại được giá trị đã ghi,
  nên chỉ có một record `FlashSaleProductDTO` và một record `FlashSaleProduct` (entity) trong project.

b) Cách kiểm chứng 50 request đồng thời

`StampedeLoadTestRunner` (`ApplicationRunner`, chỉ bật khi `flash-sale.load-test.enabled=true`) tạo
`threads` luồng cùng chờ một `CountDownLatch` rồi cùng gọi `getProduct(1)`:

```
./gradlew bootRun --args='--flash-sale.load-test.enabled=true'
```

Trong log mong đợi chỉ có một dòng `Fetching from Database for product 1` cho 50 request, cộng một dòng tổng kết
`>>> LOAD TEST: 50/50 request thanh cong trong ...ms`. Tổng thời gian xấp xỉ 2000ms + overhead, không phải
50 × 2000ms. Cách thay thế: JMeter 50 thread, ramp-up 0, cùng `GET /flash-sale/products/1`.

c) Đối chiếu bảng kiểm

| Tiêu chí | Cách kiểm |
|---|---|
| Đúng annotation | `@Cacheable(cacheNames = "flash-sale", key = "#id", sync = true)` - `FlashSaleStampedeTest` chốt bằng reflection |
| Một lần truy vấn DB | chỉ một dòng `Fetching from Database for product 1` trong log khi bật load test |
| Không timeout | runner log `50/50 request thanh cong`; không có exception nào bị `future.get()` ném ra |
| Cơ chế lock hoạt động | đổi `sync = true` thành `sync = false` (hoặc bỏ hẳn) rồi chạy lại: log sẽ có nhiều dòng `Fetching from Database for product 1` |
| Thời gian xử lý | dòng `>>> LOAD TEST: ... trong ~2000ms` |

Giới hạn cần nói rõ: lời hứa "chỉ một truy vấn DB" được đo trên MỘT instance - đó là phạm vi mà
`CacheInterceptor` + khoá cache bảo đảm. Khi có N instance, khoá `~lock` trên Redis làm luồng của instance khác
chờ và đọc lại, nên thực tế thường vẫn là một lần nạp cho một key - nhưng đó là hệ quả của khoá có TTL
(sleepTime mặc định 50ms, khoá có thể hết hạn giữa hai lần kiểm tra), không phải bảo đảm. Muốn đúng một lần
trên toàn hệ thống thì phải dùng khoá phân tán (Redisson `RLock`).

Chưa chạy build/test trong môi trường này (theo yêu cầu), nên kết quả trên chưa được xác nhận bằng output thật.
