package client;

import utility.ServiceLog;
import utility.ServiceRMI;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientRMI implements Client{
    private String serverHost;
    private int serverPort = -1;
    private ServiceRMI service;
    private ExecutorService threadPool = Executors.newFixedThreadPool(10);

    /**
     * Set hostname to the server
     * @param serverHost hostname of the server to be set
     */
    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    /**
     * Set port number to the server
     * @param serverPort port number of the server to be set
     */
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public void connect() {
        try {
            service = (ServiceRMI) LocateRegistry.getRegistry(serverHost, serverPort).lookup("ServiceRMI");
        } catch (Exception e) {
            ServiceLog.warnLog("ClientRMI start error!\n" + e.getMessage());
        }
    }

    @Override
    public String send(String request) {
        try {
            ServiceLog.infoLog("Send request: " + request);
            String response = service.process(request);
            ServiceLog.infoLog("Get response: " + response);
            return response;
        } catch (RemoteException e) {
            ServiceLog.warnLog("ClientRMI send request error!\n" + e.getMessage());
        }
        return "";
    }

    @Override
    public void close() {

    }

    public static void main (String[] args) {
        if (args.length < 2) {
            ServiceLog.warnLog("Pleas input both sever hostname/IP and port number.");
            System.exit(1);
        }

        String ip = args[0];
        int port = Integer.parseInt(args[1]);

        ClientRMI client = new ClientRMI();
        client.setServerHost(ip);
        client.setServerPort(port);
        client.connect();

        System.out.println(welcome);

        for (String request : commands) {
            client.send(request);
        }

        Scanner scanner = new Scanner (System.in);
        while (true) {
            String request = scanner.nextLine();
            client.send(request);
        }
    }
}
