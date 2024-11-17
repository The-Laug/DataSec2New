package org.example.Shared;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PrintService extends Remote{
    String print() throws RemoteException;


    String[] authenticate(String username, String clientResponse) throws RemoteException;

}

