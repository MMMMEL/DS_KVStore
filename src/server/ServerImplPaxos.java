package server;

import client.ClientPaxos;
import utility.*;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class ServerImplPaxos implements ServerPaxos {
    private int serverPort = -1;
    private StorePaxos store = new StorePaxos();
    private Set<Integer> peers = new HashSet<>();
    private Map<Integer, ClientPaxos> connections = new HashMap<>();
    private Set<Integer> faultConnections = new HashSet<>();
    private ServerMessage prevPermission = new ServerMessage(); /** the previous granted permission if any */
    /** prevPermission need to be synced with service if separate service from server */

    public ServerImplPaxos () {}

    public int getServerPort() {
        return serverPort;
    }

    public Set<Integer> getPeers() {
        return peers;
    }

    public StorePaxos getStore() {
        return store;
    }

    public void setPort(int port) {
        this.serverPort = port;
    }

    public void setStore(StorePaxos store) {
        this.store = store;
    }

    /**
     * Catch up with peer servers for information
     * about highest suggestion ID and previous value
     */
    private void gainInformation () {
        for (int port : peers) {
            try {
                connect (port);
                ServiceLog.infoLog("Connected to " + port);
                ServerMessage request = new ServerMessage();
                request.setSource(serverPort);
                request.setStatus(MessagePaxos.Status.RESTART);
                ServerMessage response = connections.get(port).send(request);
                ServiceLog.infoLog("Get RESTART response from " + port);
                if (response != null &&
                        response.getPrevPermission().getSuggestionID() > prevPermission.getSuggestionID()) {
                    prevPermission = response.getPrevPermission();
                    store = response.getStore();
                }
            }
            catch (Exception e) {
                ServiceLog.infoLog("Server " + port + " is not available.");
            }
        }
    }

    private void connect (int port) throws Exception {
        ClientPaxos connection = new ClientPaxos("localhost", port);
        connection.connect();
        connections.put (port, connection);
    }

    public void start() {
        if (serverPort == -1) {
            ServiceLog.warnLog("Server port number is missing...");
            return;
        }
        ServiceLog.infoLog("Gaining information from peers...");
        gainInformation();
        try {
            ServerPaxos stub = (ServerPaxos) UnicastRemoteObject.exportObject(this, serverPort);
            ServiceLog.infoLog("create registry...");
            Registry registry = LocateRegistry.createRegistry(serverPort);
            ServiceLog.infoLog("bind service...");
            registry.bind("ServerPaxos", stub);
            ServiceLog.infoLog("Server " + serverPort + " started with Paxos Protocol!");
        } catch (Exception e) {
            ServiceLog.warnLog("ServerPaxos " + serverPort + " start error!\n" + e.getMessage());
        }
    }

    @Override
    public ClientMessage process(ClientMessage request) throws RemoteException {
        ServiceLog.infoLog("Start to process client request: "+
                request.getAction() + " " + request.getKey() +
                (request.getAction() == MessagePaxos.Action.PUT ? (" " + request.getValue()) : ""));
        ClientMessage response =
                request.getAction() == MessagePaxos.Action.GET ? processGet(request) : processUpdate(request);
        return response;
    }

    private ClientMessage processGet(ClientMessage request) {
        return (ClientMessage) store.process(request);
    }

    private ClientMessage processUpdate(ClientMessage request) {
        //Request for permission from peers
        ServiceLog.infoLog("Proposal multicasting to peers...");
        ServerMessage proposal = new ServerMessage (request);
        proposal.setSource(serverPort);
        long suggestionID = prevPermission.getSuggestionID() + serverPort % (peers.size() + 1) + 1;
        proposal.setSuggestionID(suggestionID);
        proposal.setStatus(MessagePaxos.Status.PREPARE);
        List<ServerMessage> responses = new ArrayList<>();
        HashSet<Integer> ports = new HashSet<>(connections.keySet());
        for (int port : ports) {
            try {
                connect(port);
                ClientPaxos connection = connections.get(port);
                ServerMessage response = connection.send(proposal);
                if (response != null) {
                    responses.add(response);
                }
            } catch (Exception e) {
                ServiceLog.infoLog("Server " + port + " is not available.");
                faultConnections.add(port);
                connections.remove(port);
            }
        }

        int promised = 0;
        for (ServerMessage response : responses) {
            if (response.getStatus() == MessagePaxos.Status.PROMISE) {
                promised++;
            }
        }

        if (responses.size() == 0 || responses.size() != 0 && promised <= responses.size() / 2) {
            //proposal should be aborted
            ServiceLog.infoLog("Proposal will be aborted...");
            request.setResult(MessagePaxos.Result.FAILED);
            return request;
        }

        //Request for commitment of update
        ServiceLog.infoLog("Commit multicasting...");
        ServerMessage commitRequest = new ServerMessage(proposal);
        commitRequest.setStatus(MessagePaxos.Status.ACCEPT);
        commitRequest.setSource(serverPort);
        //update commit request suggestiongID and value;
        if (request.getAction() == MessagePaxos.Action.PUT) {
            for (ServerMessage response : responses) {
                if (response.getValue() != null &&
                        response.getSuggestionID() > commitRequest.getSuggestionID()) {
                    commitRequest.setSuggestionID(response.getSuggestionID());
                    commitRequest.setValue(response.getValue());
                }
            }
        }

        ports = new HashSet<>(connections.keySet());
        for (int port : ports) {
            ClientPaxos connection = connections.get(port);
            try {
                connection.send(commitRequest);
            } catch (RemoteException re) {
                ServiceLog.infoLog("Server " + port + " can not process commit request.");
                faultConnections.add(port);
                connections.remove(port);
            }
        }

        prevPermission.setSuggestionID(commitRequest.getSuggestionID());
        return new ClientMessage(store.process(request));
    }

    @Override
    public ServerMessage process(ServerMessage message) throws RemoteException {
        switch (message.getStatus()) {
            case RESTART:
                ServiceLog.infoLog("Process RESTART request from server " + message.getSource() + "...");
                faultConnections.remove(message.getSource());
                connections.put(message.getSource(), null);
                message.setStore(store);
                message.setPrevPermission(new ServerMessage(prevPermission));
                ServiceLog.infoLog("Finished RESTART request!");
                break;
            case PREPARE:
                ServiceLog.infoLog("Received Prepared Proposal from " + message.getSource() + "!");
                if ((new Random().nextInt(10) < 5)) {
                    ServiceLog.warnLog("Server " + serverPort + " acceptor is down!");
                    return null;
                }
                if (message.getSuggestionID() > prevPermission.getSuggestionID()) {
                    message.setStatus(MessagePaxos.Status.PROMISE);
                    if (message.getAction() == MessagePaxos.Action.PUT &&
                            prevPermission.getKey() != null && message.getKey() == prevPermission.getKey() &&
                            prevPermission.getValue() != null) {
                        message.setValue(prevPermission.getValue());
                    }
                    prevPermission = new ServerMessage(message);
                }
                break;
            case ACCEPT:
                ServiceLog.infoLog("Received Accepted Request from " + message.getSource() + "!");
                if (message.getSuggestionID() == prevPermission.getSuggestionID()) {
                    prevPermission.setValue(message.getValue());
                    store.process(message);
                }
                break;
            default:
                break;
        }
        return message;
    }

    public static void main (String[] args) {
        if (args.length != 5) {
            ServiceLog.warnLog("Pleas input 5 port numbers.");
            System.exit(1);
        }
        ServerImplPaxos server = new ServerImplPaxos();
        server.setPort(Integer.parseInt(args[0]));

        for (int i = 1; i < 5; i++) {
            server.peers.add(Integer.parseInt(args[i]));
        }
        server.start();
    }
}
