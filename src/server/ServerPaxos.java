package server;

import utility.*;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerPaxos extends Remote {
    ClientMessage process (ClientMessage message) throws RemoteException;

    ServerMessage process (ServerMessage message) throws RemoteException;
}
