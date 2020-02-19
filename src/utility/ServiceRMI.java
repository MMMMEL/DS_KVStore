package utility;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServiceRMI extends Remote {
    public String process (String request) throws RemoteException;
}
