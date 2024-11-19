package org.example.Shared.Impl;

import org.example.Shared.AuthenticationService;
import org.example.Shared.PrintService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PrintServiceImpl extends UnicastRemoteObject implements PrintService {
    private final AuthenticationService AuthenticationService;
    private final Map<String, List<String>> printerQueues = new ConcurrentHashMap<>();

    protected PrintServiceImpl(AuthenticationService authenticationService) throws RemoteException {
        this.AuthenticationService = authenticationService;
    }


    @Override
    public void print(String filename, String printer, String token) throws RemoteException {
        validateToken(token);
        printerQueues.putIfAbsent(printer, new ArrayList<>());
        printerQueues.get(printer).add(filename);
        System.out.println("Printing the file: " + filename + " on printer " + printer);
    }



    private void validateToken(String token) throws RemoteException {
        if (!AuthenticationService.validateToken(token)) {
            throw new RemoteException("Invalid token");
        }
    }
}