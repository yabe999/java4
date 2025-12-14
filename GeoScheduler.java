package exp;

import java.util.*;
import java.util.stream.Collectors;

public class GeoScheduler {

    /**
     * 返回 3 km 内且直线距离最近的 Runner，无命中返回 null
     * @param runners 候选跑腿员
     * @param cargo   货物（商家）坐标
     * @param maxKm   最大半径（3 km）
     */
    public static Runner assignNearest(List<Runner> runners, Location cargo, double maxKm) {
        if (cargo == null) return null; // 没录坐标就回落原逻辑

        return runners.stream()
                .filter(r -> "空闲".equals(r.getStatus()))
                .filter(r -> r.getLocation() != null)
                .filter(r -> DistanceUtil.km(cargo, r.getLocation()) <= maxKm)
                .min(Comparator.comparingDouble(r -> DistanceUtil.km(cargo, r.getLocation())))
                .orElse(null);   // 3 km 内无人 → 回落随机分配
    }

    /**
     * 仅返回 3 km 内且直线距离最近的订单（用于前端弹窗排序）
     */
    public static List<Order> nearestOrders(List<Order> orders, Location cargo, double maxKm) {
        return orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING)
                .filter(o -> o.getCargoLocation() != null)
                .sorted(Comparator.comparingDouble(o -> DistanceUtil.km(cargo, o.getCargoLocation())))
                .filter(o -> DistanceUtil.km(cargo, o.getCargoLocation()) <= maxKm)
                .collect(Collectors.toList());
    }
}