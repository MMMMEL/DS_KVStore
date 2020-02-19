package server;

import utility.ServiceLog;
import utility.ServiceRMI;
import utility.StoreRMI;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ServerRMI implements Server {
    private int serverPort = -1;

    public void setPort(int port) {
        this.serverPort = port;
    }

    @Override
    public void start() {
        if (serverPort == -1) {
            ServiceLog.warnLog("Server port number is missing...");
            return;
        }
        try {
            ServiceRMI store = new StoreRMI();
            ServiceRMI stub = (ServiceRMI) UnicastRemoteObject.exportObject(store, serverPort);
            Registry registry = LocateRegistry.createRegistry(serverPort);
            registry.bind("ServiceRMI", stub);
            ServiceLog.infoLog("Server started with RMI service!");
        }
        catch (Exception e) {
            ServiceLog.warnLog("ServerRMI start error!\n" + e.getMessage());
        }
    }

    @Override
    public void terminate() {

    }

    public static void main (String[] args) {
        if (args.length < 1) {
            ServiceLog.warnLog("Pleas input both sever port number.");
            System.exit(1);
        }

        ServerRMI server = new ServerRMI();
        server.setPort(Integer.parseInt(args[0]));
        server.start();
    }
}
