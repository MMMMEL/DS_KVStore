package client;


import utility.ServiceLog;

import java.net.*;
import java.util.Scanner;

public class ClientUDP implements Client{

    private DatagramSocket socket;
    private InetAddress ip;
    private int serverPort = -1; //need to be set

    /**
     * Set hostname to the server
     * @param hostname hostname of the server to be set
     */
    public void setIp(String hostname) {
        try {
            this.ip = InetAddress.getByName(hostname);
        } catch (Exception e){
            ServiceLog.warnLog("Invalid hostname!");
        }

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
            socket = new DatagramSocket();
        } catch (SocketException e) {
            ServiceLog.warnLog("Cannot create a datagram socket.");
        }
    }

    @Override
    public String send (String request) {
        if (ip == null) {
            ServiceLog.warnLog("Server IP address is missing...");
            return "";
        }

        if (serverPort == -1) {
            ServiceLog.warnLog("Server port number is missing...");
            return "";
        }
        try {
            ServiceLog.infoLog("Send request: " + request);
            byte[] buf = request.getBytes();
            DatagramPacket packet =
                    new DatagramPacket(buf, buf.length, ip, serverPort);

            socket.send(packet);
            socket.setSoTimeout(10 * 1000);

            byte[] responseBuffer = new byte[256];
            DatagramPacket reply = new DatagramPacket(responseBuffer, responseBuffer.length);
            socket.receive(reply);
            String response = new String(reply.getData(), 0, reply.getLength());
            ServiceLog.infoLog("Get Response: " + response);
            return response;
        } catch (SocketTimeoutException te) {
            ServiceLog.warnLog("Server connection timeout!");
        } catch (Exception e) {
            ServiceLog.warnLog(e.getMessage());
        }
        return "";
    }

    @Override
    public void close() {
        if (socket != null) {
            socket.close();
        }
    }

    public static void main (String[] args) {
        if (args.length < 2) {
            ServiceLog.warnLog("Pleas input both sever hostname/IP and port number.");
            System.exit(1);
        }

        String ip = args[0];
        int port = Integer.parseInt(args[1]);

        ClientUDP client = new ClientUDP();
        client.setIp(ip);
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
