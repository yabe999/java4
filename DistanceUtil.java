package exp;

public final class DistanceUtil {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /** 球面距离（km），仅用于范围判断 */
    public static double km(Location a, Location b) {
        double lat1 = Math.toRadians(a.getLatitude());
        double lon1 = Math.toRadians(a.getLongitude());
        double lat2 = Math.toRadians(b.getLatitude());
        double lon2 = Math.toRadians(b.getLongitude());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(h));
    }
}
