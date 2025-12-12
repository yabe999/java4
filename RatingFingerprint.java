package exp;

import java.util.concurrent.*;

public final class RatingFingerprint {
    private static final ConcurrentHashMap<String, Byte> CACHE = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

    static {
        // 每 30 秒批量清理过期键（简单时间轮）
        SCHEDULER.scheduleWithFixedDelay(() -> {
            long now = System.currentTimeMillis();
            CACHE.entrySet().removeIf(entry -> now - Long.parseLong(entry.getKey().split("@")[1]) > 30_000);
        }, 30, 30, TimeUnit.SECONDS);
    }

    /**
     * 30 秒内同一指纹不能重复评价
     * @param fingerprint 推荐格式：userIP + orderId + "@" + System.currentTimeMillis()
     */
    public static void assertNotRepeated(String fingerprint) {
        if (CACHE.putIfAbsent(fingerprint, (byte) 1) != null)
            throw new RepeatRatingException("30 秒内不能重复评价");
    }

    public static void shutdown() {
        SCHEDULER.shutdown();
    }
}