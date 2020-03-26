package utility;

import client.Client2PC;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Define a class that implements the Service2PC interface
 */
public class ServiceImpl2PC implements Service2PC{
    private Store2PC store;
    private Store2PC cache; // a cache of key-value store for preparing changes
    private List<Integer> ports; // list of port numbers of other servers
    private List<Client2PC> connections = new ArrayList<>(); // list of clients used to connect other servers

    public ServiceImpl2PC (Store2PC store, List<Integer> ports) {
        this.store = store;
        this.ports = ports;
    }

    @Override
    public Message process(Message request) throws RemoteException {
        ServiceLog.infoLog("Start to process client request: " +
                request.getAction() + " " + request.getKey() +
                (request.getAction() == Message.Action.PUT ? (" " + request.getValue()) : ""));
        Message response =
                request.getAction() == Message.Action.GET ? processGet(request) : processUpdate(request);
        return response;
    }

    /**
     * Process GET request
     * @param request request message
     * @return response message
     */
    private Message processGet(Message request) {
        return store.process(request);
    }

    /**
     * Process PUT and DELETE request
     * @param message request/response message
     * @return response message
     */
    private Message processUpdate(Message message) {
        if (connections.size() != ports.size()) {
            ServiceLog.infoLog("Set up connections to peer servers...");
            connectPeerServers();
            ServiceLog.infoLog("Finished set up peer connections.");
        }

        if (message.getSource() == Message.Source.CLIENT) {
            ServiceLog.infoLog("Process an update request from a client...");
            //initiate a list of response messages from other servers
            List<Message> responses = new ArrayList<>();
            //set message source to SERVER and pass it to other peer servers
            message.setSource(Message.Source.SERVER);

            List<Client2PC> faultConnections = new ArrayList<>();
            //get all responses from other servers
            for (Client2PC connection : connections) {
                try {
                    Message response = connection.send(message);
                    responses.add(response);
                } catch (RemoteException e){
                    ServiceLog.warnLog("Server " + connection.getServerPort() + " is down. Failed to get response.");
                    faultConnections.add(connection);
                    Coordinator.getInstance().restartServer(connection.getServerPort());
                }
            }
            removeFaultConnections(faultConnections);

            //check all responses from other servers
            for (Message response : responses) {
                //deal with an abort response
                if (response.getStatus() == Message.Status.ABORTED) {
                    message.setStatus(Message.Status.ABORTED);
                    //send message about request abortion to other servers again
                    for (Client2PC connection : connections) {
                        try {
                            connection.send(message);
                        } catch (RemoteException e){
                            ServiceLog.warnLog("Server " + connection.getServerPort() + " is down.");
                            faultConnections.add(connection);
                        }
                    }
                    return message;
                }
            }
            removeFaultConnections(faultConnections);

            //no aborted response, continue to commit
            message.setStatus(Message.Status.COMMITTED);
            for (Client2PC connection : connections) {
                try {
                    connection.send(message);
                }catch (RemoteException e){
                    ServiceLog.warnLog("Server " + connection.getServerPort() + " is down.");
                    faultConnections.add(connection);
                }
            }
            removeFaultConnections(faultConnections);
            //current server process request and return response
            Message res = store.process(message);
            Coordinator.getInstance().setStore(new Store2PC(store.getStore()));
            ServiceLog.infoLog("Coordinator store has entry: " + Coordinator.getInstance().getData().size());
            return res;
        } else { //if the message is from other servers
            ServiceLog.infoLog("Process an update request from a peer server...");
            ServiceLog.infoLog("Request status is: " + message.getStatus());
            switch (message.getStatus()) {
                //if message is just initialized, create a cache copy to process the request
                case INITIALIZED:
                    cache = store.copy();
                    return cache.process(message);
                //if message is committed, replace store with cache copy and reset cache
                case COMMITTED:
                    store = cache;
                    cache = null;
                    break;
                //if message is aborted, abort the changes in cache copy
                case ABORTED:
                    cache = null;
                    break;
            }
            return message;
        }
    }

    private void removeFaultConnections(List<Client2PC> faultConnections) {
        if (faultConnections.size() > 0) {
            for (Client2PC connection : faultConnections) {
                connections.remove(connection);
            }
        }
        faultConnections.clear();
    }

    /**
     * Create connections with other servers
     */
    private void connectPeerServers() {
        connections = new ArrayList<>();
        for (int port : ports) {
            Client2PC client = new Client2PC("localhost", port);
            try {
                client.connect();
                connections.add(client);
            } catch (Exception e) {
                ServiceLog.warnLog("Server " + port + " is down.");
            }
        }
    }
}
