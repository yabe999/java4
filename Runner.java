package exp;

import java.io.Serializable;

public class Runner implements Serializable {
    private final String name;
    private final String phone;
    private String status = "空闲";   // 空闲 / 忙碌
    /* ========== 第四层：坐标 ========== */
    private Location location;
    /* =================================== */

    public Runner(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }
    public String getName() { return name; }
    public String getStatus() { return status; }
    public String getPhone() {
        return phone;
    }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public boolean takeOrder() {
        if ("忙碌".equals(status)) return false;
        status = "忙碌";
        return true;
    }
    public void completeOrder() {
        status = "空闲";
    }
    @Override
    public String toString() {
        return name + "[" + status + "]";
    }
}