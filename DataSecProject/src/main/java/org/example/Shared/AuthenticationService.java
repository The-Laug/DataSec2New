package org.example.Shared;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AuthenticationService extends Remote {
    String[] authenticate(String username, String clientResponse) throws RemoteException, IOException;

    boolean validateToken(String token) throws RemoteException;
}
