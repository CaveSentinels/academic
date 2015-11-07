import java.util.Date;

public class EquipmentInfo {

    private String name;
    private String description;
    private long lastSeenTime;  // The time that this equipment was last seen.

    public EquipmentInfo(String n, String d) {
        name = n;
        description = d;
        lastSeenTime = System.currentTimeMillis();
    }

    public String GetName() {
        return name;
    }

    public String GetDescription() {
        return description;
    }

    public void Heartbeat() { lastSeenTime = System.currentTimeMillis(); }

    public long GetLastSeenTime() { return lastSeenTime; }
}
