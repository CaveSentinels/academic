import java.util.HashMap;

public class KeyValueLib {

    public static HashMap<String, Integer> dataCenters =
            new HashMap<String, Integer>();

    public static void PUT(String db, String key, String value) {
        // Just mock-up
        System.out.println(db + key + value);
    }

    public static String GET(String db, String key) {
        // Just mock-up
        return db + key;
    }
}
