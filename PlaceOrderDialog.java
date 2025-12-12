package exp;

import javax.swing.*;
import java.awt.*;

public class PlaceOrderDialog extends JDialog {
    public PlaceOrderDialog(Window owner) {
        super(owner, "下单", ModalityType.APPLICATION_MODAL);
        setSize(400, 380);                       // 增高给坐标输入留空间
        setLocationRelativeTo(owner);

        JPanel p = new JPanel(new GridLayout(0, 1, 5, 5));
        JTextField descF   = new JTextField();
        JCheckBox  urgentC = new JCheckBox("紧急");
        JComboBox<Student> stuBox = new JComboBox<>(Main.students.toArray(new Student[0]));

        // 第四层：商家坐标
        JTextField latF = new JTextField("22.543210");
        JTextField lngF = new JTextField("113.123456");

        p.add(new JLabel("订单描述")); p.add(descF);
        p.add(new JLabel("商家纬度")); p.add(latF);
        p.add(new JLabel("商家经度")); p.add(lngF);
        p.add(new JLabel("学生"));     p.add(stuBox);
        p.add(urgentC);

        JButton ok = new JButton("确定");
        ok.addActionListener(e -> {
            if (descF.getText().isBlank() || stuBox.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "请完整输入");
                return;
            }
            try {
                double lat = Double.parseDouble(latF.getText().trim());
                double lng = Double.parseDouble(lngF.getText().trim());
                Location cargoLoc = new Location(lat, lng);

                Order o = new Order(descF.getText(), (Student) stuBox.getSelectedItem(), urgentC.isSelected());
                o.setCargoLocation(cargoLoc);   // 关键：存货物坐标
                Main.orders.add(o);
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "纬度/经度必须是数字！");
            }
        });
        add(p, BorderLayout.CENTER);
        add(ok, BorderLayout.SOUTH);
    }
}