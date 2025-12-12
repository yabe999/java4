package exp;

public final class DistanceUtil {
    private static final double EARTH_RADIUS = 6371; // km

    public static double km(Location a, Location b) {
        double rad = Math.toRadians(b.getLatitude() - a.getLatitude());
        double lng = Math.toRadians(b.getLongitude() - a.getLongitude());
        double lat1 = Math.toRadians(a.getLatitude());
        double lat2 = Math.toRadians(b.getLatitude());

        double h = Math.sin(rad / 2) * Math.sin(rad / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(lng / 2) * Math.sin(lng / 2);
        return 2 * EARTH_RADIUS * Math.asin(Math.sqrt(h));
    }
}