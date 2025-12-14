package exp;

import java.util.List;

public final class MerchantLocationPool {
    /* 3 km 网格点（约 1.2 km 格子） */
    private static final Location[] POOL = {
            new Location(22.543210, 113.123456),
            new Location(22.538000, 113.120000),
            new Location(22.541000, 113.126000),
            new Location(22.536000, 113.118000),
            new Location(22.544000, 113.128000)
    };

    /** 返回 3 km 内商家坐标（以学生为圆心） */
    public static List<Location> within3km(Location student) {
        return java.util.Arrays.stream(POOL)
                .filter(m -> DistanceUtil.km(student, m) <= 3.0)
                .toList();
    }
}