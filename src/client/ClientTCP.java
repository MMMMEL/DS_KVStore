package client;

import utility.ServiceLog;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;

public class ClientTCP implements Client{

    private Socket socket;
    private String serverHost;
    private int serverPort = -1;

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
        if (serverHost == null) {
            ServiceLog.warnLog("Server hostname / IP address is missing...");
            return;
        }
        if (serverPort == -1) {
            ServiceLog.warnLog("Server port number is missing...");
            return;
        }
        try {
            socket = new Socket(serverHost, serverPort);
            socket.setSoTimeout(10 * 1000);
        } catch (SocketTimeoutException te) {
            ServiceLog.warnLog("Server connection timeout!");
        } catch (Exception e) {
            ServiceLog.warnLog("Cannot connect server at " + serverPort + "!");
        }
    }

    @Override
    public String send(String request) {
        if (socket == null) {
            ServiceLog.warnLog("Client socket is not connected!");
        }
        try {
            // Get client request and send to server socket
            DataOutputStream dos = new DataOutputStream (socket.getOutputStream());
            dos.writeUTF(request);
            ServiceLog.infoLog("Send request: " + request);

            // Get an input file handle from the socket and read the input
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            String response = new String (dis.readUTF());
            ServiceLog.infoLog("Get response: " + response);
            dos.close();
            dis.close();
            return response;
        } catch (IOException e) {
            ServiceLog.warnLog(e.getMessage());
        }
        return "";
    }

    @Override
    public void close() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                ServiceLog.warnLog("Client socket cannot be closed!");
            }
        }
    }

    public static void main (String[] args) {
        if (args.length < 2) {
            ServiceLog.warnLog("Pleas input both sever hostname/IP and port number.");
            System.exit(1);
        }

        String ip = args[0];
        int port = Integer.parseInt(args[1]);


        ClientTCP client = new ClientTCP();
        client.setServerHost(ip);
        client.setServerPort(port);

        System.out.println(welcome);

        for (String request : commands) {
            client.connect();
            client.send(request);
            client.close();
        }

        Scanner scanner = new Scanner (System.in);
        while (true) {
            String request = scanner.nextLine();
            client.connect();
            client.send(request);
            client.close();
        }
    }
}
