package utility;

import java.util.HashMap;
import java.util.Map;

public class Store2PC {
    private Map<String, String> store;

    public Store2PC() {
        store = new HashMap<>();

        store.put("k1", "v1");
        store.put("k2", "v2");
        store.put("k3", "v3");
        store.put("k4", "v4");
        store.put("k5", "v5");
    }

    public Store2PC(Map<String, String> store) {
        this.store = new HashMap<>(store);
    }

    public Map<String, String> getStore() {
        return store;
    }

    public synchronized Message process (Message message) {
        switch (message.getAction()) {
            case GET:
                if (!store.containsKey(message.getKey())) {
                    message.setValue(null);
                    message.setResult(Message.Result.FAILED);
                } else {
                    message.setValue(store.get(message.getKey()));
                    message.setResult(Message.Result.SUCCEEDED);
                }
                break;
            case PUT:
                store.put(message.getKey(), message.getValue());
                message.setResult(Message.Result.SUCCEEDED);
                break;
            case DELETE:
                if (!store.containsKey(message.getKey())) {
                    message.setResult(Message.Result.FAILED);
                } else {
                    store.remove(message.getKey());
                    message.setResult(Message.Result.SUCCEEDED);
                }
                break;
        }
        message.setStatus(Message.Status.COMMITTED);
        message.setSource(Message.Source.SERVER);
        return message;
    }

    public Store2PC copy() {
        return new Store2PC(store);
    }
}
