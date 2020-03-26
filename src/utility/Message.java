package utility;

import java.io.Serializable;

/**
 * This class define the message transmitted between client and servers or among servers
 * A message can be a request or a response
 */
public class Message implements Serializable {

    /**
     * Define the Action for request contained in a Message
     */
    public enum Action {GET, PUT, DELETE};


    /**
     * Define the status of Message
     * INITIALIZED: a request is started
     * COMMITTED: a request is committed
     * ABORTED: a request is aborted
     */
    public enum Status {INITIALIZED, COMMITTED, ABORTED};

    /**
     * Define the result of an action committed
     * SUCCEEDED: committed successfully
     * FAILED: committed failed
     */
    public enum Result {SUCCEEDED, FAILED, PENDING};

    /**
     * Define the Message source
     * CLIENT: Message is sent by a client
     * SERVER: Message is sent by a server
     */
    public enum Source {CLIENT, SERVER};

    private String key; /** key of key-value pair */
    private String value; /** value of key-value pair */
    private Action action; /** action on the key-value pair */
    private Status status = Status.INITIALIZED; /** status of message */
    private Result result = Result.PENDING;
    private Source source = Source.CLIENT; /** if message is from client or server */

    public Message () {}

    public Message (String key, String value, Action action) {
        this.key = key;
        this.value = value;
        this.action = action;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
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
