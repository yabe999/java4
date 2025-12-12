package exp;

public final class RatingService {
    private RatingService() {}

    /**
     * 对订单打五星
     * @param order 目标订单
     * @param score 0-5 整数
     * @param fingerprint 调用者指纹（已含时间戳）
     */
    public static void rate(Order order, int score, String fingerprint) {
        if (order.isRated())
            throw new RepeatRatingException("该订单已评价，不可重复评分");
        if (score < 0 || score > 5)
            throw new IllegalArgumentException("评分必须在 0-5 之间");
        RatingFingerprint.assertNotRepeated(fingerprint); // ← 只传 1 个参数
        order.setScore(score);
        order.setRated(true);
    }
}