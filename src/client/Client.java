package client;

public interface Client {
    String[] commands = new String[] {
            "PUT k1 v1",
            "PUT k2 v2",
            "PUT k3 v3",
            "GET k1",
            "GET k2",
            "DELETE k1",
            "GET k1",
            "GET k2",
            "GET k3",
            "PUT k4 v4",
            "PUT k5 v5",
            "GET k4",
            "GET k5",
            "GET k1 k2",
            "GET",
            "DELETE k1 k2",
            "PUT k6",
            "GET k6",
            "DELETE k2",
            "DELETE k3",
            "DELETE k4",
            "DELETE k5",
    };

    String welcome = "Welcome to Key-Value Store!\n" +
                    "Service currently supported: \n" +
                    "PUT key value\n" +
                    "GET key\n" +
                    "DELETE key\n" +
                    "Please follow the format and have fun!";

    /**
     * Create a socket for Client.
     * Connection to a specific server may be built.
     */
    void connect();

    /**
     * Send a request to a server and get a response or timeout
     * @param request the request client will send
     * @return response returned back from server
     */
    String send (String request);

    /**
     * Close the Client
     */
    void close();
}
