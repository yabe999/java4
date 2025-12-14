package exp;

public final class GeoHash {
    private static final String BASE_32 = "0123456789bcdefghjkmnpqrstuvwxyz";
    private static final int[] BITS = {16, 8, 4, 2, 1};

    /** 将经纬度编码为 GeoHash（精度 12 位，约 3.7cm） */
    public static String encode(double lat, double lng) {
        return encode(lat, lng, 12);
    }

    private static String encode(double lat, double lng, int precision) {
        StringBuilder geohash = new StringBuilder();
        boolean even = true;
        int bit = 0, ch = 0;
        double minLat = -90,  maxLat = 90;
        double minLng = -180, maxLng = 180;

        while (geohash.length() < precision) {
            double mid;
            if (even) {
                mid = (minLng + maxLng) / 2;
                if (lng > mid) { ch |= BITS[bit]; minLng = mid; } else maxLng = mid;
            } else {
                mid = (minLat + maxLat) / 2;
                if (lat > mid) { ch |= BITS[bit]; minLat = mid; } else maxLat = mid;
            }
            even = !even;
            if (bit < 4) { bit++; } else {
                geohash.append(BASE_32.charAt(ch));
                bit = 0; ch = 0;
            }
        }
        return geohash.toString();
    }

    /** 计算两个 GeoHash 的前缀长度（越长越近） */
    public static int commonPrefixLength(String a, String b) {
        int min = Math.min(a.length(), b.length());
        for (int i = 0; i < min; i++) {
            if (a.charAt(i) != b.charAt(i)) return i;
        }
        return min;
    }
}