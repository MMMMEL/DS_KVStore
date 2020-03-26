package utility;

import server.Server2PC;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Coordinator {
    private Store2PC store;
    private static Coordinator instance = null;
    int[] ports = new int[] {1111, 2222, 3333, 4444, 5555};
    List<Server2PC> servers = new ArrayList<>();

    private Coordinator () {
        this.store = new Store2PC();
    }

    public Store2PC getStore() {
        return this.store;
    }

    public void setStore(Store2PC store) {
        if (instance == null) {
            getInstance();
        }
        instance.store = store;
    }

    public Map<String, String> getData() {
        if (instance == null) {
            getInstance();
        }
        return instance.getStore().getStore();
    }

    public static synchronized Coordinator getInstance() {
        if (instance == null) {
            ServiceLog.infoLog("Coordinator instance is null!");
            instance = new Coordinator();
        }
        return instance;
    }

    public void restartServer (int port) {
        for (Server2PC server : servers) {
            if (server.getServerPort() == port) {
                server.setCoordinator(instance);
                server.setStore(new Store2PC(instance.getData()));
                ServiceLog.infoLog("Check coordinator store entry: " + instance.getData().size());
                ServiceLog.infoLog("Server got a store with entry: " + server.getStore().getStore().size());

                server.setPort(port);
                server.restart();
                break;
            }
        }
    }

    public void stopServer (int port) {
        for (Server2PC server : servers) {
            if (server.getServerPort() == port) {
                try {
                    ServiceLog.infoLog("Exiting server " + port + "...");
                    server.exit();
                    ServiceLog.infoLog("Server " + port + " is exited.");
                } catch (RemoteException e) {
                    ServiceLog.warnLog("Server " + port + " exit error!\n" + e.getMessage());
                }

                break;
            }
        }
    }
    public static void main (String[] args) throws InterruptedException {
        Coordinator coordinator = Coordinator.getInstance();

        for (int i = 0; i < 5; i++) {
            Server2PC server = new Server2PC();
            server.setCoordinator(coordinator);
            server.setStore(new Store2PC(coordinator.getData()));
            ServiceLog.infoLog("Check coordinator store entry: " + coordinator.getData().size());
            ServiceLog.infoLog("Server got a store with entry: " + server.getStore().getStore().size());

            server.setPort(coordinator.ports[i]);
            for (int j = 0; j < 5; j++) {
                if (j != i) {
                    server.getPeers().add(coordinator.ports[j]);
                }
            }
            server.start();
            coordinator.servers.add(server);
        }

        Thread.sleep(45 * 1000);
        coordinator.stopServer(3333);

        Thread.sleep(20 * 1000);
        coordinator.restartServer(3333);
    }
}