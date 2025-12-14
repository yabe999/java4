package exp;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class OrderTableModel extends AbstractTableModel {
    private final List<Order> data;
    private final String[] cols = {
            "订单号", "描述", "状态", "学生", "跑腿员", "紧急", "创建时间", "评分", "距离(km)"
    };

    public OrderTableModel(List<Order> data) { this.data = data; }

    public void refresh() { fireTableDataChanged(); }

    @Override public int getRowCount() { return data.size(); }
    @Override public int getColumnCount() { return cols.length; }
    @Override public String getColumnName(int c) { return cols[c]; }

    @Override
    public Object getValueAt(int r, int c) {
        Order o = data.get(r);
        switch (c) {
            case 0: return o.getOrderId();
            case 1: return o.getDesc();
            case 2:
                switch (o.getStatus()) {
                    case PENDING:    return "待接单";
                    case DELIVERING: return "配送中";
                    case COMPLETED:  return "已完成";
                    case CANCELED:   return "已取消";
                    case TIMEOUT:    return "超时";
                    default:         return o.getStatus().toString();
                }
            case 3: return o.getStudent().getName();
            case 4: return o.getRunner() == null ? "" : o.getRunner().getName();
            case 5: return o.isUrgent() ? "是" : "否";
            case 6: return o.getCreateTime().toString();
            case 7: return o.isRated() ? "⭐" + o.getScore() : "未评";
            case 8: // 距离列：配送中使用百度API距离，其他使用球面距离
                Location cargo = o.getCargoLocation();
                Location stu = o.getStudent().getLocation();
                if (cargo == null || stu == null) return "-";

                // 状态判断：配送中使用百度地图真实驾驶距离
                if (o.getStatus() == OrderStatus.DELIVERING) {
                    // 调用百度API获取驾驶距离（米）
                    int meter = BaiduMapService.meter(stu, cargo);
                    if (meter >= 0) {
                        return String.format("%.2f km (百度)", meter / 1000.0);
                    } else {
                        // API调用失败时 fallback 到球面距离
                        double km = DistanceUtil.km(cargo, stu);
                        return String.format("%.2f km (备用)", km);
                    }
                } else {
                    // 非配送中状态使用原球面直线距离
                    double km = DistanceUtil.km(cargo, stu);
                    return String.format("%.2f km", km);
                }
            default: return "";
        }
    }
}