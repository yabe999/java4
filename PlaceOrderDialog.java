package exp;

import javax.swing.*;
import java.awt.*;

public class PlaceOrderDialog extends JDialog {

    private final JTextField descF = new JTextField();
    private final JTextField latF  = new JTextField();  // 商家纬度
    private final JTextField lngF  = new JTextField();  // 商家经度
    // 新增：学生坐标显示字段
    private final JTextField stuLatF = new JTextField(); // 学生纬度
    private final JTextField stuLngF = new JTextField(); // 学生经度
    private final JCheckBox urgentC = new JCheckBox("紧急");
    private final JComboBox<Student> stuBox =
            new JComboBox<>(Main.students.toArray(new Student[0]));
    private final JLabel hintLbl = new JLabel("请先定位学生");

    private final RangeMapPanel mapPanel = new RangeMapPanel();

    public PlaceOrderDialog(Window owner, Location studentLoc) {
        super(owner, "下单", ModalityType.APPLICATION_MODAL);
        setSize(720, 420);  // 适当增大窗口高度以容纳新字段
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // 设置字段不可编辑
        latF.setEditable(false);
        lngF.setEditable(false);
        stuLatF.setEditable(false);  // 学生坐标不可编辑
        stuLngF.setEditable(false);

        JPanel p = new JPanel(new GridLayout(8, 2, 6, 6));  // 调整网格行数为8

        // 添加原有字段
        p.add(new JLabel("订单描述："));
        p.add(descF);
        // 新增：学生坐标行
        p.add(new JLabel("学生纬度："));
        p.add(stuLatF);
        p.add(new JLabel("学生经度："));
        p.add(stuLngF);
        // 原有商家坐标行
        p.add(new JLabel("商家纬度："));
        p.add(latF);
        p.add(new JLabel("商家经度："));
        p.add(lngF);
        p.add(new JLabel("学生："));
        p.add(stuBox);
        p.add(new JLabel("紧急订单："));
        p.add(urgentC);
        p.add(hintLbl);
        p.add(new JLabel());  // 占位，平衡网格

        JButton locBtn = new JButton("定位（我）");
        JButton okBtn  = new JButton("确定");

        JPanel btnP = new JPanel();
        btnP.add(locBtn);
        btnP.add(okBtn);

        add(p, BorderLayout.CENTER);
        add(mapPanel, BorderLayout.EAST);
        add(btnP, BorderLayout.SOUTH);

        // 定位按钮事件：添加学生坐标回填
        locBtn.addActionListener(e -> {
            Student stu = (Student) stuBox.getSelectedItem();
            if (stu == null) return;

            Location h = BaiduMapService.getCurrentLocation();
            if (h == null) {
                JOptionPane.showMessageDialog(this, "定位失败");
                return;
            }

            stu.setLocation(h);
            mapPanel.setStudent(h);
            // 回填学生坐标到文本框
            stuLatF.setText(String.format("%.6f", h.getLatitude()));
            stuLngF.setText(String.format("%.6f", h.getLongitude()));
            hintLbl.setText("点击圆内任意位置选择商家");
        });

        mapPanel.addPropertyChangeListener("merchant", evt -> {
            Location m = (Location) evt.getNewValue();
            latF.setText(String.format("%.6f", m.getLatitude()));
            lngF.setText(String.format("%.6f", m.getLongitude()));
            hintLbl.setText("商家已选中（3 km 内）");
            hintLbl.setForeground(new Color(0, 128, 0));
        });

        okBtn.addActionListener(e -> {
            // 原有逻辑不变...
            if (descF.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "请输入订单描述");
                return;
            }

            Student stu = (Student) stuBox.getSelectedItem();
            if (stu == null || stu.getLocation() == null) {
                JOptionPane.showMessageDialog(this, "请先定位学生");
                return;
            }

            Location cargo = mapPanel.getMerchant();
            if (cargo == null) {
                JOptionPane.showMessageDialog(this, "请在圆内选择商家");
                return;
            }

            Order o = new Order(descF.getText(), stu, urgentC.isSelected());
            o.setCargoLocation(cargo);
            Main.orders.add(o);
            dispose();
        });
    }
}