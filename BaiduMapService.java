package exp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public final class BaiduMapService {
    private static final String AK = "rNYlX5yKyCnQrl66Pat71vVKTAeOZzWN"; // 你的 AK

    /** 返回真实驾驶距离（米），失败返回 -1 */
    public static int meter(Location from, Location to) {
        try {
            // 关键修复：添加 coord_type=bd09ll 声明坐标系为BD-09
            String url = "https://api.map.baidu.com/directionlite/v1/driving?" +
                    "origin=" + from.getLatitude() + "," + from.getLongitude() +
                    "&destination=" + to.getLatitude() + "," + to.getLongitude() +
                    "&coord_type=bd09ll" +  // 明确指定坐标系为BD-09
                    "&ak=" + AK;
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            if (conn.getResponseCode() != 200) return -1;

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line, json = "";
            while ((line = in.readLine()) != null) json += line;
            in.close();
            conn.disconnect();

            int idx = json.indexOf("\"distance\"");
            if (idx == -1) return -1;
            int start = json.indexOf(':', idx) + 1;
            int end   = json.indexOf(',', start);
            return Integer.parseInt(json.substring(start, end).trim());
        } catch (Exception e) {
            return -1;
        }
    }

    /** 返回当前 IP 的大致坐标（失败返回 null） */
    public static Location getCurrentLocation() {
        try (java.util.Scanner s = new java.util.Scanner(
                new URL("https://api.map.baidu.com/location/ip?ak=" + AK + "&coor=bd09ll")
                        .openStream(), "UTF-8")) {
            String json = s.useDelimiter("\\A").next();
            int xIdx = json.indexOf("\"x\":\"");
            int yIdx = json.indexOf("\"y\":\"");
            if (xIdx == -1 || yIdx == -1) return null;
            double lng = Double.parseDouble(json.substring(xIdx + 5, json.indexOf('"', xIdx + 5)));
            double lat = Double.parseDouble(json.substring(yIdx + 5, json.indexOf('"', yIdx + 5)));
            return new Location(lat, lng);
        } catch (Exception e) {
            return null;
        }
    }
}