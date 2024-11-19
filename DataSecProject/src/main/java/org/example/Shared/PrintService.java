package org.example.Shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface PrintService extends Remote {
    void print(String token, String filename, String printer) throws RemoteException;

    List<String> queue(String token, String printer) throws RemoteException;

    void topQueue(String token, String printer, int job) throws RemoteException;

    // Other printer functionalities...
}
