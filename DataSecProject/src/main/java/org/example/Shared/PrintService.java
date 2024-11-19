package org.example.Shared;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PrintService extends Remote {
    void print(String filename, String printer,String token) throws RemoteException;


}

