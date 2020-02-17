package server;

import utility.Service;

public interface Server {
    Service service = new Service();
    /**
     * Start a server
     */
    void start();

    /**
     * Terminate a server
     */
    void terminate();
}
