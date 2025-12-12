package exp;

import java.io.Serializable;

public class Student implements Serializable {
    private final String id;
    private final String name;
    private final String phone;
    private final String major;
    /* ========== 第四层：坐标 ========== */
    private Location location;
    /* =================================== */

    public Student(String id, String name, String phone, String major) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.major = major;
    }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getMajor() {
        return major;
    }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    @Override
    public String toString() {
        return name + "[" + major + "]";
    }
}