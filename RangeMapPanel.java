package exp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RangeMapPanel extends JPanel {

    private Location student;   // BD-09
    private Location merchant;  // BD-09

    private static final int RADIUS_PX = 120;
    private static final double RADIUS_KM = 3.0;

    public RangeMapPanel() {
        setPreferredSize(new Dimension(260, 260));
        setBackground(Color.WHITE);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (student == null) return;

                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                int dx = e.getX() - cx;
                int dy = cy - e.getY();

                double distPx = Math.sqrt(dx * dx + dy * dy);
                if (distPx > RADIUS_PX) return; // 圆外无效

                double kmPerPx = RADIUS_KM / RADIUS_PX;
                double dxKm = dx * kmPerPx;
                double dyKm = dy * kmPerPx;

                double dLat = dyKm / 111.0;
                double dLng = dxKm /
                        (111.0 * Math.cos(Math.toRadians(student.getLatitude())));

                merchant = new Location(
                        student.getLatitude() + dLat,
                        student.getLongitude() + dLng
                );

                repaint();
                firePropertyChange("merchant", null, merchant);
            }
        });
    }

    public void setStudent(Location s) {
        this.student = s;
        this.merchant = null;
        repaint();
    }

    public Location getMerchant() {
        return merchant;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (student == null) return;

        int cx = getWidth() / 2;
        int cy = getHeight() / 2;

        g.setColor(new Color(200, 220, 255));
        g.fillOval(cx - RADIUS_PX, cy - RADIUS_PX,
                RADIUS_PX * 2, RADIUS_PX * 2);

        g.setColor(Color.BLUE);
        g.fillOval(cx - 5, cy - 5, 10, 10);

        if (merchant != null) {
            int dx = (int) ((merchant.getLongitude() - student.getLongitude())
                    * Math.cos(Math.toRadians(student.getLatitude())) * 111 / RADIUS_KM * RADIUS_PX);
            int dy = (int) ((merchant.getLatitude() - student.getLatitude())
                    * 111 / RADIUS_KM * RADIUS_PX);

            g.setColor(Color.RED);
            g.fillOval(cx + dx - 5, cy - dy - 5, 10, 10);
        }
    }
}
