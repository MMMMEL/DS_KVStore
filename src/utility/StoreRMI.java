package utility;

import java.rmi.RemoteException;

public class StoreRMI implements ServiceRMI {
    static Service service = new Service();

    @Override
    public String process(String request) throws RemoteException {
        return service.process(request);
    }
}
