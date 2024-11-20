package org.example.Shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface PrintService extends Remote {
    void print(String token, String filename, String printer) throws RemoteException;
    List<String> queue(String token, String printer) throws RemoteException;

    void topQueue(String token, String printer, int job) throws RemoteException;

    void stop(String token) throws RemoteException;

    void start(String token) throws RemoteException;

    void restart(String token) throws RemoteException;
    // Other printer functionalities...

    void status(String token,String printer) throws RemoteException;

    void readConfig(String token,String parameter) throws RemoteException;

    void setConfig(String token,String parameter,String value) throws RemoteException;
}
