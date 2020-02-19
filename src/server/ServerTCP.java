package server;

import utility.ServiceLog;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerTCP implements Server, Runnable{
    private ServerSocket serverSocket;
    private int serverPort = -1;
    private ExecutorService threadPool = Executors.newFixedThreadPool(10);

    /**
     * Set port number to the server
     * @param port the port number to be set
     */
    public void setPort(int port) {
        this.serverPort = port;
    }

    /**
     * Fulfil the request embedded in the socket
     * @param socket socket received
     */
    public void fulfilRequest (Socket socket) {
        try {
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            String order = dis.readUTF();
            String[] request = order.split(",");
            ServiceLog.infoLog("Get request from client " + request[0] + ": " + request[1]);
            String response = service.process(request[1]);
            ServiceLog.infoLog("Finish request, result: " + response);
            DataOutputStream dos = new DataOutputStream (socket.getOutputStream());
            dos.writeUTF(response);
            dis.close();
            dos.close();
        } catch (IOException e) {
            ServiceLog.warnLog("Exception thrown while fulfilling request: " + e.getMessage());
        }
    }

    @Override
    public void start() {
        if (serverPort == -1) {
            ServiceLog.warnLog("Server port number is missing...");
            return;
        }
        try {
            serverSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
            ServiceLog.warnLog("Cannot start the server at " + serverPort + "!");
        }

        while (true) {
            try {
                ServiceLog.infoLog("server sleeps");
                Thread.sleep(1000);
                ServiceLog.infoLog("server awakes");
            } catch (Exception e) {
                ServiceLog.warnLog("Thread sleep error!\n" + e.getMessage());
            }

            try {
                Socket socket = serverSocket.accept();
                socket.setSoTimeout(10 * 1000);
                this.threadPool.execute(() -> {
                    fulfilRequest (socket);
                });
            } catch (Exception e) {
                ServiceLog.warnLog(e.getMessage());
            }
        }
    }

    @Override
    public void run() {
        start();
    }

    @Override
    public void terminate() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                ServiceLog.warnLog("Cannot close the server socket!");
            }
        }
    }

    public static void main (String[] args) {
        if (args.length < 1) {
            ServiceLog.warnLog("Pleas input both sever port number.");
            System.exit(1);
        }

        ServerTCP server = new ServerTCP();
        server.setPort(Integer.parseInt(args[0]));
//        server.start();
        new Thread(server).start();
    }
}
