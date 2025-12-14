package exp;

public record RouteInfo(int distanceMeter, int durationSecond) {
    public double km() { return distanceMeter / 1000.0; }
    public String timeStr() {
        int min = durationSecond / 60;
        return min < 60 ? min + "分钟" : (min / 60) + "小时" + (min % 60) + "分钟";
    }
}