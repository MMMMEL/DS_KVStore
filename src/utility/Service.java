package utility;

import java.util.HashMap;
import java.util.Map;

public class Service {
    private Map<String, String> store;

    public Service() {
        store = new HashMap<>();

        store.put("k1", "v1");
        store.put("k2", "v2");
        store.put("k3", "v3");
        store.put("k4", "v4");
        store.put("k5", "v5");
    }

    public synchronized String process (String request) {
        ServiceLog.infoLog("Processing request: " + request);
        String[] elements = request.split(" ");
        if (elements.length < 2) {
            ServiceLog.warnLog("Invalid request with " + elements.length + " argument!");
        }
        String action = elements[0].toUpperCase();
        String key;
        String value;
        switch (action) {
            case "PUT":
                if (elements.length != 3) {
                    return "Invalid PUT request with " + elements.length + " arguments!";
                }
                key = elements[1];
                value = elements[2];
                store.put (key, value);
                return "Successfully PUT (" + key + ", " + value + ")";

            case "GET":
                if (elements.length != 2) {
                    return "Invalid GET request with " + elements.length + " arguments!";
                }
                key = elements[1];
                if (!store.containsKey(key)) {
                    return "Key \"" + key + "\" doesn't exist!";
                }
                return "Successfully GET value " + store.get(key) + " for key " + key;

            case "DELETE":
                if (elements.length != 2) {
                    return "Invalid DELETE request with " + elements.length + " arguments!";
                }
                key = elements[1];
                if (!store.containsKey(key)) {
                    return "Key \"" + key + "\" doesn't exist!";
                }
                store.remove(key);
                return "Successfully DELETE key " + key;

            default:
                return "Unsupported request: " + request;
        }
    }
}
