package org.example.Shared;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AuthenticationService extends Remote {
    String[] authenticate(String username, String clientResponse) throws Exception;

    boolean validateToken(String token) throws RemoteException;

    boolean hasPermission(String token, String permission) throws RemoteException;
}
