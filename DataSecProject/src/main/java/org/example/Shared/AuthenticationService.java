package org.example.Shared;

import java.rmi.RemoteException;

public interface AuthenticationService {

    String[] authenticate(String username, String clientResponse) throws RemoteException;

    boolean validateToken(String token);

    String getUsernameFromToken(String token);


}
