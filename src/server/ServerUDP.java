package server;

import utility.ServiceLog;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerUDP implements Server, Runnable {
    private DatagramSocket socket;
    private int serverPort = -1;
    private ExecutorService threadPool = Executors.newFixedThreadPool(10);

    /**
     * Set port number to the server
     * @param port the port number to be set
     */
    public void setPort(int port) {
        this.serverPort = port;
    }

    public void execute () {
        byte[] buffer = new byte[1000];
        DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
        try {
            socket.receive(requestPacket);
            String order = new String(requestPacket.getData(), 0, requestPacket.getLength());
            String[] request = order.split(",");
            ServiceLog.infoLog("Get request from client " + request[0] + ": " + request[1]);
            String response = service.process(request[1]);
            ServiceLog.infoLog("Get response for client " + request[0] + ": " + response);
            byte[] result = response.getBytes();
            DatagramPacket responsePacket = new DatagramPacket(result,
                    result.length,requestPacket.getAddress(),
                    requestPacket.getPort());
            socket.send(responsePacket);
        } catch (Exception e) {
            ServiceLog.warnLog(e.getMessage());
        }
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

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                ServiceLog.warnLog("Thread sleep error!\n" + e.getMessage());
            }

            this.threadPool.execute(() -> {
                execute();
            });
        }
    }

    @Override
    public void run() {
        start();
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
