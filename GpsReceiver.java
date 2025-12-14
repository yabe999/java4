package exp;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public final class GpsReceiver {

    // 实时坐标（浏览器回传）
    public static volatile double lng;
    public static volatile double lat;

    private static HttpServer server;

    public static void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(8085), 0);

            /* ================= 提供 gps.html 页面 ================= */
            server.createContext("/gps.html", ex -> {
                try (InputStream is =
                             GpsReceiver.class.getResourceAsStream("/gps.html")) {

                    if (is == null) {
                        ex.sendResponseHeaders(404, 0);
                        return;
                    }

                    byte[] data = is.readAllBytes();
                    ex.getResponseHeaders()
                            .set("Content-Type", "text/html;charset=utf-8");
                    ex.sendResponseHeaders(200, data.length);
                    ex.getResponseBody().write(data);

                } catch (IOException e) {
                    ex.sendResponseHeaders(500, 0);
                } finally {
                    ex.close();
                }
            });

            /* ================= 接收定位结果 ================= */
            server.createContext("/pos", ex -> {
                String q = ex.getRequestURI().getQuery();
                if (q != null && q.contains("lng=") && q.contains("lat=")) {
                    try {
                        lng = Double.parseDouble(
                                q.split("&")[0].split("=")[1]
                        );
                        lat = Double.parseDouble(
                                q.split("&")[1].split("=")[1]
                        );
                        System.out.println(
                                "收到 GPS 坐标 lng=" + lng + ", lat=" + lat
                        );
                    } catch (Exception ignore) {
                    }
                }

                byte[] resp = "OK".getBytes(StandardCharsets.UTF_8);
                ex.sendResponseHeaders(200, resp.length);
                ex.getResponseBody().write(resp);
                ex.close();
            });

            server.setExecutor(null); // 单线程即可
            server.start();

            System.out.println(
                    "GPS 服务已启动：http://localhost:8085/gps.html"
            );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }
}
