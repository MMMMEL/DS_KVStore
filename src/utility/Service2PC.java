package utility;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Define a Remote interface to process Message
 */
public interface Service2PC extends Remote {
    Message process (Message message) throws RemoteException;
}
