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
            case 8: // 动态三段距离
                Location cargo = o.getCargoLocation();
                if (cargo == null) return "-";

                switch (o.getStatus()) {
                    case PENDING:
                        double stuKm = o.getStudent().getLocation() == null ? -1 :
                                DistanceUtil.km(o.getStudent().getLocation(), cargo);
                        double runKm = o.getRunner() == null || o.getRunner().getLocation() == null ? -1 :
                                DistanceUtil.km(o.getRunner().getLocation(), cargo);
                        if (stuKm < 0 && runKm < 0) return "-";
                        return String.format("学生→货%.1f | 跑腿→货%.1f", stuKm, runKm);

                    case DELIVERING:
                        if (o.getStudent().getLocation() == null) return "-";
                        return String.format("货→学生%.1f",
                                DistanceUtil.km(cargo, o.getStudent().getLocation()));

                    default: // 已完成/取消
                        return "-";
                }
            default: return "";
        }
    }
}