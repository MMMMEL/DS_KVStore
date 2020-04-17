package utility;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class StorePaxos implements Serializable {

    private Map<String, String> store;

    public StorePaxos() {
        store = new HashMap<>();

        store.put("k1", "v1");
        store.put("k2", "v2");
        store.put("k3", "v3");
        store.put("k4", "v4");
        store.put("k5", "v5");
    }

    public StorePaxos(Map<String, String> store) {
        this.store = new HashMap<>(store);
    }

    public Map<String, String> getStore() {
        return store;
    }

    public synchronized MessagePaxos process (MessagePaxos message) {
        switch (message.getAction()) {
            case GET:
                if (!store.containsKey(message.getKey())) {
                    message.setValue(null);
                    message.setResult(MessagePaxos.Result.FAILED);
                } else {
                    message.setValue(store.get(message.getKey()));
                    message.setResult(MessagePaxos.Result.SUCCEEDED);
                }
                break;
            case PUT:
                store.put(message.getKey(), message.getValue());
                message.setResult(MessagePaxos.Result.SUCCEEDED);
                break;
            case DELETE:
                if (!store.containsKey(message.getKey())) {
                    message.setResult(MessagePaxos.Result.FAILED);
                } else {
                    store.remove(message.getKey());
                    message.setResult(MessagePaxos.Result.SUCCEEDED);
                }
                break;
        }
        return message;
    }

    public StorePaxos copy() {
        return new StorePaxos(store);
    }
}
