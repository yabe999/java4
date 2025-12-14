package exp;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class OrderPanel extends JPanel {
    private final OrderTableModel model;
    private final JTable table;

    public OrderPanel() {
        super(new BorderLayout());
        model = new OrderTableModel(Main.orders);
        table = new JTable(model);

        /* ===== 表格基础设置 ===== */
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(8).setPreferredWidth(180);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                if (value != null) setToolTipText(value.toString());
                return c;
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        /* ===== 状态列着色 ===== */
        TableColumn statusCol = table.getColumnModel().getColumn(2);
        statusCol.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                if (!isSelected && value != null) {
                    switch (value.toString()) {
                        case "待接单" -> c.setBackground(new Color(0xE3F2FD));
                        case "配送中" -> c.setBackground(new Color(0xFFF9C4));
                        case "已完成" -> c.setBackground(new Color(0xC8E6C9));
                        case "已取消", "超时" -> c.setBackground(new Color(0xFFCDD2));
                        default -> c.setBackground(Color.WHITE);
                    }
                }
                c.setForeground(Color.BLACK);
                return c;
            }
        });
        statusCol.setPreferredWidth(80);

        /* ===== 按钮栏 ===== */
        JPanel bar = new JPanel();
        bar.add(new JButton(new PlaceOrderAction()));
        bar.add(new JButton(new AssignOrderAction()));
        bar.add(new JButton(new CompleteOrderAction()));
        bar.add(new JButton(new CancelOrderAction()));
        bar.add(new JButton(new WithdrawOrderAction(table)));
        bar.add(new JButton(new DeleteOrderAction()));
        bar.add(new JButton(new RateOrderAction(table)));
        bar.add(new JButton(new ManualSaveAction()));
        bar.add(new JButton(new RefreshAction()));
        bar.add(new JButton(new ShowRouteAction(table)));
        add(bar, BorderLayout.NORTH);

        /* ===== 日志区 ===== */
        JTextArea log = new JTextArea(3, 0);
        log.setEditable(false);
        log.setText("提示：每5min自动保存，每30s检测超时订单。");
        AutoSaveManager.logArea = log;
        TimeoutManager.logArea = log;
        add(new JScrollPane(log), BorderLayout.SOUTH);
    }

    /* ================= 下单（GPS 版） ================= */
    private class PlaceOrderAction extends AbstractAction {
        PlaceOrderAction() { super("下单"); }

        @Override
        public void actionPerformed(ActionEvent e) {

            GpsReceiver.lat = 0;
            GpsReceiver.lng = 0;

            showGpsWeb();

            new SwingWorker<Location, Void>() {

                @Override
                protected Location doInBackground() throws Exception {
                    int waited = 0;
                    while (waited < 30) {
                        if (GpsReceiver.lat != 0 && GpsReceiver.lng != 0) {
                            return new Location(GpsReceiver.lat, GpsReceiver.lng);
                        }
                        Thread.sleep(500);
                        waited++;
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        Location real = get();
                        if (real == null) {
                            JOptionPane.showMessageDialog(
                                    OrderPanel.this,
                                    "GPS 定位超时，请重试。",
                                    "提示",
                                    JOptionPane.WARNING_MESSAGE
                            );
                            return;
                        }

                        new PlaceOrderDialog(
                                SwingUtilities.windowForComponent(OrderPanel.this),
                                real
                        ).setVisible(true);
                        model.refresh();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }.execute();
        }
    }

    /* ================= 打开浏览器 ================= */
    private void showGpsWeb() {
        try {
            Desktop.getDesktop().browse(new URI("http://localhost:8085/gps.html"));
            JOptionPane.showMessageDialog(
                    this,
                    "浏览器已打开，请允许定位，完成后可关闭网页。",
                    "GPS 定位",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "无法打开浏览器：" + ex.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /* ================= 分配订单 ================= */
    private class AssignOrderAction extends AbstractAction {
        AssignOrderAction() { super("分配订单"); }

        @Override
        public void actionPerformed(ActionEvent e) {
            List<Order> pending = Main.orders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.PENDING)
                    .collect(Collectors.toList());
            if (pending.isEmpty()) {
                JOptionPane.showMessageDialog(OrderPanel.this, "暂无待分配订单！");
                return;
            }

            Order selectedOrder = (Order) JOptionPane.showInputDialog(
                    OrderPanel.this, "选择待分配订单：",
                    "分配订单", JOptionPane.QUESTION_MESSAGE,
                    null, pending.toArray(), pending.get(0)
            );
            if (selectedOrder == null) return;

            List<Runner> availableRunners = Main.runners.stream()
                    .filter(r -> !"忙碌".equals(r.getStatus()))
                    .collect(Collectors.toList());
            if (availableRunners.isEmpty()) {
                JOptionPane.showMessageDialog(OrderPanel.this, "暂无可用跑腿员！");
                return;
            }

            Runner selectedRunner = (Runner) JOptionPane.showInputDialog(
                    OrderPanel.this, "选择跑腿员：",
                    "分配订单", JOptionPane.QUESTION_MESSAGE,
                    null, availableRunners.toArray(), availableRunners.get(0)
            );
            if (selectedRunner == null) return;

            if (selectedRunner.takeOrder()) {
                selectedOrder.setRunner(selectedRunner);
                selectedOrder.changeStatus(OrderStatus.DELIVERING);
                JOptionPane.showMessageDialog(OrderPanel.this, "订单分配成功！");
                model.refresh();
            } else {
                JOptionPane.showMessageDialog(OrderPanel.this, "分配失败，跑腿员忙碌中！");
            }
        }
    }

    /* ================= 完成订单 ================= */
    private class CompleteOrderAction extends AbstractAction {
        CompleteOrderAction() { super("完成订单"); }

        @Override
        public void actionPerformed(ActionEvent e) {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(OrderPanel.this, "请先选择要完成的订单！");
                return;
            }
            Order order = Main.orders.get(row);
            if (order.getStatus() != OrderStatus.DELIVERING) {
                JOptionPane.showMessageDialog(OrderPanel.this, "只有【配送中】订单可完成！");
                return;
            }

            int ok = JOptionPane.showConfirmDialog(
                    OrderPanel.this,
                    "确认订单 " + order.getOrderId() + " 已完成？",
                    "完成确认",
                    JOptionPane.YES_NO_OPTION
            );
            if (ok != JOptionPane.YES_OPTION) return;

            order.changeStatus(OrderStatus.COMPLETED);
            if (order.getRunner() != null) {
                order.getRunner().completeOrder();
            }
            JOptionPane.showMessageDialog(OrderPanel.this, "订单已完成！");
            model.refresh();
        }
    }

    /* ================= 取消订单 ================= */
    private class CancelOrderAction extends AbstractAction {
        CancelOrderAction() { super("取消订单"); }

        @Override
        public void actionPerformed(ActionEvent e) {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(OrderPanel.this, "请先选择订单！");
                return;
            }
            Order order = Main.orders.get(row);
            if (order.getStatus() != OrderStatus.PENDING &&
                    order.getStatus() != OrderStatus.DELIVERING) {
                JOptionPane.showMessageDialog(OrderPanel.this, "只有【待接单/配送中】可取消！");
                return;
            }

            int ok = JOptionPane.showConfirmDialog(
                    OrderPanel.this,
                    "确认取消订单 " + order.getOrderId() + "？",
                    "取消确认",
                    JOptionPane.YES_NO_OPTION
            );
            if (ok != JOptionPane.YES_OPTION) return;

            order.changeStatus(OrderStatus.CANCELED);
            if (order.getRunner() != null) {
                order.getRunner().completeOrder();
            }
            JOptionPane.showMessageDialog(OrderPanel.this, "订单已取消！");
            model.refresh();
        }
    }

    /* ================= 删除订单 ================= */
    private class DeleteOrderAction extends AbstractAction {
        DeleteOrderAction() { super("删除订单"); }

        @Override
        public void actionPerformed(ActionEvent e) {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(OrderPanel.this, "请选择订单！");
                return;
            }

            Order order = Main.orders.get(row);
            OrderStatus st = order.getStatus();
            boolean canDel = (st == OrderStatus.PENDING && order.getRunner() == null)
                    || st == OrderStatus.COMPLETED
                    || st == OrderStatus.CANCELED;

            if (!canDel) {
                JOptionPane.showMessageDialog(OrderPanel.this, "当前订单不可删除！");
                return;
            }

            int ok = JOptionPane.showConfirmDialog(
                    OrderPanel.this,
                    "确认删除订单 " + order.getOrderId() + "？",
                    "删除确认",
                    JOptionPane.YES_NO_OPTION
            );
            if (ok != JOptionPane.YES_OPTION) return;

            Main.orders.remove(order);
            model.refresh();
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() {
                    Main.saveData();
                    return null;
                }
            }.execute();
        }
    }

    /* ================= 评价订单 ================= */
    private class RateOrderAction extends AbstractAction {
        private final JTable table;
        RateOrderAction(JTable table) { super("评价"); this.table = table; }

        @Override
        public void actionPerformed(ActionEvent e) {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(OrderPanel.this, "请选择订单！");
                return;
            }
            Order order = Main.orders.get(row);
            if (order.getStatus() != OrderStatus.COMPLETED || order.isRated()) {
                JOptionPane.showMessageDialog(OrderPanel.this, "订单不可评价！");
                return;
            }

            JDialog dlg = new JDialog(
                    SwingUtilities.windowForComponent(OrderPanel.this),
                    "订单评价",
                    Dialog.ModalityType.APPLICATION_MODAL
            );
            dlg.add(new RatingPanel(order));
            dlg.pack();
            dlg.setLocationRelativeTo(OrderPanel.this);
            dlg.setVisible(true);
            model.refresh();
        }
    }

    /* ================= 手动保存 ================= */
    private class ManualSaveAction extends AbstractAction {
        ManualSaveAction() { super("手动保存"); }
        @Override public void actionPerformed(ActionEvent e) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() {
                    Main.saveData();
                    return null;
                }
                @Override protected void done() {
                    JOptionPane.showMessageDialog(OrderPanel.this, "保存完成！");
                }
            }.execute();
        }
    }

    /* ================= 刷新 ================= */
    private class RefreshAction extends AbstractAction {
        RefreshAction() { super("刷新"); }
        @Override public void actionPerformed(ActionEvent e) {
            model.refresh();
        }
    }

    /* ================= 查看路线 ================= */
    private class ShowRouteAction extends AbstractAction {
        private static final String AK = "rNYlX5yKyCnQrl66Pat71vVKTAeOZzWN";
        ShowRouteAction(JTable table) { super("查看路线"); }

        @Override
        public void actionPerformed(ActionEvent e) {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(OrderPanel.this, "请选择订单！");
                return;
            }
            Order order = Main.orders.get(row);

            Location s = order.getStudent().getLocation();
            Location m = order.getCargoLocation();
            if (s == null || m == null) {
                JOptionPane.showMessageDialog(OrderPanel.this, "坐标缺失！");
                return;
            }

            String url = "https://api.map.baidu.com/staticimage/v2"
                    + "?ak=" + AK
                    + "&width=600&height=400"
                    + "&markers="
                    + s.getLongitude() + "," + s.getLatitude() + "|"
                    + m.getLongitude() + "," + m.getLatitude();

            try {
                BufferedImage img = ImageIO.read(new URL(url));
                JOptionPane.showMessageDialog(
                        OrderPanel.this,
                        new JLabel(new ImageIcon(img)),
                        "路线预览",
                        JOptionPane.PLAIN_MESSAGE
                );
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(OrderPanel.this, "地图加载失败！");
            }
        }
    }
}
