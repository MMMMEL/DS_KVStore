package utility;

public class ServerMessage extends MessagePaxos {
    private Status status = Status.PREPARE;
    private long suggestionID = -1;

    private int source;
    private StorePaxos store;
    private ServerMessage prevPermission;

    public ServerMessage () {}

    public ServerMessage (ClientMessage message) {
        super (message);
    }

    public ServerMessage (ServerMessage message) {
        super (message);
        status = message.status;
        suggestionID = message.suggestionID;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public long getSuggestionID() {
        return suggestionID;
    }

    public void setSuggestionID(long suggestionID) {
        this.suggestionID = suggestionID;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public StorePaxos getStore() {
        return store;
    }

    public void setStore(StorePaxos store) {
        this.store = store;
    }

    public ServerMessage getPrevPermission() {
        return prevPermission;
    }

    public void setPrevPermission(ServerMessage prevPermission) {
        this.prevPermission = prevPermission;
    }
}
