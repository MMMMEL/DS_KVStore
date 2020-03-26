package server;

import utility.Service2PC;
import utility.ServiceImpl2PC;
import utility.ServiceLog;
import utility.Store2PC;
import utility.Coordinator;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class Server2PC {
    private int serverPort = -1;
    private List<Integer> peers = new ArrayList<>();
    private static Coordinator coordinator;
    private Store2PC store;
    private Service2PC service;

    public Server2PC() {
    }

    public int getServerPort() {
        return serverPort;
    }

    public List<Integer> getPeers() {
        return peers;
    }

    public Coordinator getCoordinator() {
        return coordinator;
    }

    public Store2PC getStore() {
        return store;
    }

    public void setPort(int port) {
        this.serverPort = port;
    }

    public void setCoordinator(Coordinator coordinator) {
        this.coordinator = coordinator;
    }

    public void setStore(Store2PC store) {
        this.store = store;
    }

    public void start() {
        if (serverPort == -1) {
            ServiceLog.warnLog("Server port number is missing...");
            return;
        }
        try {
            service = new ServiceImpl2PC(store, peers);
            Service2PC stub = (Service2PC) UnicastRemoteObject.exportObject(service, serverPort);
            ServiceLog.infoLog("create registry...");
            Registry registry = LocateRegistry.createRegistry(serverPort);
            ServiceLog.infoLog("bind service...");
            registry.bind("Service2PC", stub);
            ServiceLog.infoLog("Server " + serverPort + " started with 2PC Protocol!");
        } catch (Exception e) {
            ServiceLog.warnLog("Server2PC " + serverPort + " start error!\n" + e.getMessage());
        }
    }

    public void restart() {
        try {
            service = new ServiceImpl2PC(store, peers);
            Service2PC stub = (Service2PC) UnicastRemoteObject.exportObject(service, serverPort);
            Registry registry = LocateRegistry.getRegistry("localhost", serverPort);
            ServiceLog.infoLog("rebind service...");
            registry.rebind("Service2PC", stub);
            ServiceLog.infoLog("Server " + serverPort + " restarted with 2PC Protocol!");
        } catch (Exception e) {
            ServiceLog.warnLog("Server2PC " + serverPort + " restart error!\n" + e.getMessage());
        }
    }

    public void exit() throws RemoteException {
        Registry registry = LocateRegistry.getRegistry("localhost", serverPort);
        try {
            ServiceLog.infoLog("unbind service...");
            registry.unbind("Service2PC");
            ServiceLog.infoLog("unexport object...");
            UnicastRemoteObject.unexportObject(service, true);
        } catch (NotBoundException e) {
            throw new RemoteException("Could not unregister service, quiting anyway", e);
        }
    }
}
