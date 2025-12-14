package exp;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainFrame extends JFrame {
    private static final ScheduledExecutorService pool = Executors.newScheduledThreadPool(2);

    public MainFrame() {
        super("校园跑腿系统 - 创新层");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        /* ---------- 数据加载 ---------- */
        Main.loadOnStartup();          // 沿用第二层加载逻辑

        /* ---------- 启动 GPS 坐标接收 ---------- */
        GpsReceiver.start();           // ← 仅启动一次

        /* ---------- 界面 ---------- */
        JTabbedPane tab = new JTabbedPane();
        tab.add("订单管理", new OrderPanel());
        tab.add("用户管理", new UserPanel());
        add(tab);

        /* ---------- 后台线程 ---------- */
        AutoSaveManager asm = new AutoSaveManager();
        TimeoutManager tm  = new TimeoutManager();
        pool.scheduleWithFixedDelay(asm, 0, 5, TimeUnit.MINUTES);
        pool.scheduleWithFixedDelay(tm,  0, 30, TimeUnit.SECONDS);

        /* ---------- 优雅退出 ---------- */
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                GpsReceiver.stop();   // 释放 8085 端口
                pool.shutdown();
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}