package exp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class RatingPanel extends JPanel {
    private final Order order;
    private final JSlider starSlider = new JSlider(0, 5, 3);
    private final JLabel starLabel = new JLabel("⭐⭐⭐");

    public RatingPanel(Order order) {
        this.order = order;
        // 用 GridBagLayout 避免被撑开
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        starSlider.setMajorTickSpacing(1);
        starSlider.setPaintTicks(true);
        starSlider.setPaintLabels(true);
        updateLabel(starSlider.getValue());
        starSlider.addChangeListener(e -> updateLabel(starSlider.getValue()));

        gbc.gridx = 0; gbc.gridy = 0; add(new JLabel("评分："), gbc);
        gbc.gridx = 1; add(starSlider, gbc);
        gbc.gridx = 2; add(starLabel, gbc);

        JButton submit = new JButton("提交");
        submit.addActionListener(this::submitRating);
        gbc.gridx = 1; gbc.gridy = 1; add(submit, gbc);

        setPreferredSize(new Dimension(400, 120)); // 固定大小，防止撑爆
    }

    private void updateLabel(int val) {
        starLabel.setText("⭐".repeat(val));
    }

    private void submitRating(ActionEvent e) {
        try {
            String fingerprint = "user_" + System.currentTimeMillis() + "_order_" + order.getOrderId();
            RatingService.rate(order, starSlider.getValue(), fingerprint);
            JOptionPane.showMessageDialog(this, "感谢评分！");
            ((JButton) e.getSource()).setEnabled(false);
            // 关闭窗口
            SwingUtilities.getWindowAncestor(this).dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "评分失败", JOptionPane.WARNING_MESSAGE);
        }
    }
}