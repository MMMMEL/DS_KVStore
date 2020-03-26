package client;

import utility.Message;
import utility.Service2PC;
import utility.ServiceLog;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;

public class Client2PC {

    private String serverHost;
    private int serverPort;
    private Service2PC service;

    public Client2PC () {}

    public Client2PC (String host, int port) {
        this.serverHost = host;
        this.serverPort = port;
    }

    public int getServerPort() {
        return serverPort;
    }
    /**
     * Set hostname to the application.server
     * @param serverHost hostname of the application.server to be set
     */
    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    /**
     * Set port number to the application.server
     * @param serverPort port number of the application.server to be set
     */
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public void connect() throws Exception{
        service = (Service2PC) LocateRegistry.getRegistry(serverHost, serverPort).lookup("Service2PC");
    }

    public Message send(Message request) throws RemoteException {
//        try {
        Message response = service.process(request);
        return response;
//        } catch (RemoteException e) {
//            ServiceLog.warnLog("Client2PC send request error!\n" + e.getMessage());
//        }
//        return null;
    }

    private static Message generateMessage(String command) {
        String[] elements = command.split(" ");
        if (elements.length < 2) {
            ServiceLog.warnLog("Invalid request with " + elements.length + " argument!");
            return null;
        }
        String action = elements[0].toUpperCase();
        Message message = new Message();

        switch (action) {
            case "PUT":
                if (elements.length != 3) {
                    ServiceLog.warnLog("Invalid PUT request with " + elements.length + " arguments!");
                    return null;
                }
                message.setAction(Message.Action.PUT);
                message.setKey(elements[1]);
                message.setValue(elements[2]);
                break;
            case "GET":
                if (elements.length != 2) {
                    ServiceLog.warnLog("Invalid GET request with " + elements.length + " arguments!");
                    return null;
                }
                message.setAction(Message.Action.GET);
                message.setKey(elements[1]);
                break;
            case "DELETE":
                if (elements.length != 2) {
                    ServiceLog.warnLog("Invalid DELETE request with " + elements.length + " arguments!");
                    return null;
                }
                message.setAction(Message.Action.DELETE);
                message.setKey(elements[1]);
                break;
            default:
                ServiceLog.warnLog("Unsupported request: " + command);
                return null;
        }

        return message;
    }

    public static void main (String[] args) {
        final String welcome = "Welcome to Key-Value Store!\n" +
                "Service currently supported: \n" +
                "PUT key value\n" +
                "GET key\n" +
                "DELETE key\n" +
                "Please follow the format and have fun!";

        if (args.length < 2) {
            ServiceLog.warnLog("Pleas input both sever hostname/IP and port number.");
            System.exit(1);
        }

        String ip = args[0];
        int port = Integer.parseInt(args[1]);

        Client2PC client = new Client2PC();
        client.setServerHost(ip);
        client.setServerPort(port);
        try{
            client.connect();
        } catch (Exception e) {
            ServiceLog.warnLog("Client2PC connection to " + client.serverHost + " " + client.serverPort + " start error!\n"
                    + e.getMessage());
        }

        System.out.println(welcome);

        Scanner scanner = new Scanner (System.in);
        while (true) {
            String command = scanner.nextLine();
            Message request = generateMessage(command);
            if (request != null) {
                try {
                    ServiceLog.infoLog("Sending request: " + command);
                    Message response = client.send(request);
                    ServiceLog.infoLog(response.getMessage());
                } catch (RemoteException e) {
                    ServiceLog.warnLog("Client2PC send request error!\n" + e.getMessage());
                }
            }
        }
    }
}
