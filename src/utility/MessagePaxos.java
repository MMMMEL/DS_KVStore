package utility;

import java.io.Serializable;

public abstract class MessagePaxos implements Serializable {
    /**
     * Define the Action for request contained in a Message
     */
    public enum Action {GET, PUT, DELETE};

    /**
     * Define the status of Message
     * PREPARE: a proposal is prepared by a server
     * PROMISE: a proposal is promised by a server
     * ACCEPT: a proposal is accepted by majority of peers
     */
    public enum Status {PREPARE, PROMISE, ACCEPT, RESTART};

    /**
     * Define the result of an action committed
     * SUCCEEDED: committed successfully
     * FAILED: committed failed
     */
    public enum Result {SUCCEEDED, FAILED, PENDING};

    private String key; /** key of key-value pair */
    private String value; /** value of key-value pair */
    private Action action; /** action on the key-value pair */
    private Result result = Result.PENDING;

    public MessagePaxos () {}

    public MessagePaxos (MessagePaxos message) {
        this.key = message.key;
        this.value = message.value;
        this.action = message.action;
        this.result = message.result;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public String getMessage() {
        switch (action) {
            case GET:
                return result + " to " + action + " " +
                        key + "'s value " + (value == null ? "" : value);
            case PUT:
                return result + " to " + action + " (" + key + ", " + value + ")";
            case DELETE:
                return result + " to " + action + " " + key;
        }
        return "";
    }
}
