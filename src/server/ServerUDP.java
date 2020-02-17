package server;

import utility.ServiceLog;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ServerUDP implements Server{

    private DatagramSocket socket;
    private int serverPort = -1;

    /**
     * Set port number to the server
     * @param port the port number to be set
     */
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
            socket = new DatagramSocket (serverPort);
        } catch (SocketException e) {
            ServiceLog.warnLog("Cannot start the server at " + serverPort + "!");
        }
        byte[] buffer = new byte[1000];
        while (true) {
            DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(requestPacket);
                String request = new String(requestPacket.getData(), 0, requestPacket.getLength());
                ServiceLog.infoLog("Get request: " + request);
                String response = service.process(request);
                ServiceLog.infoLog("Get response: " + response);
                byte[] result = response.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(result,
                        result.length,requestPacket.getAddress(),
                        requestPacket.getPort());
                socket.send(responsePacket);
            } catch (Exception e) {
                ServiceLog.warnLog(e.getMessage());
            }
        }
    }

    @Override
    public void terminate() {
        if (socket != null) {
            socket.close();
        }
    }

    public static void main (String[] args) {
        if (args.length < 1) {
            ServiceLog.warnLog("Pleas input both sever port number.");
            System.exit(1);
        }

        ServerUDP server = new ServerUDP();
        server.setPort(Integer.parseInt(args[0]));
        server.start();
    }
}
