package exp;

import java.io.Serializable;

public class Location implements Serializable {
    private final double latitude;   // BD-09
    private final double longitude;  // BD-09

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return String.format("%.6f, %.6f", latitude, longitude);
    }
}
