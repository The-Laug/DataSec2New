package org.example.Server;

import org.example.Shared.AuthenticationService;
import org.example.Shared.PrintService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class PrintServiceImpl extends UnicastRemoteObject implements PrintService {
    private final AuthenticationService authService;
    private final Map<String, List<String>> printerQueues = new ConcurrentHashMap<>();

    protected PrintServiceImpl(AuthenticationService authService) throws RemoteException {
        this.authService = authService;
    }

    @Override
    public void print(String token, String filename, String printer) throws RemoteException {
        validateToken(token);
        printerQueues.putIfAbsent(printer, new ArrayList<>());
        printerQueues.get(printer).add(filename);
        System.out.println("Printed file " + filename + " from printer " + printer);
    }

    @Override
    public List<String> queue(String token, String printer) throws RemoteException {
        validateToken(token);
        return printerQueues.getOrDefault(printer, Collections.emptyList());
    }

    @Override
    public void topQueue(String token, String printer, int job) throws RemoteException {
        validateToken(token);
        List<String> queue = printerQueues.get(printer);
        if (queue != null && job >= 0 && job < queue.size()) {
            String jobFile = queue.remove(job);
            queue.add(0, jobFile);
        }
    }

    private void validateToken(String token) throws RemoteException {
        if (!authService.validateToken(token)) {
            throw new RemoteException("Invalid or expired token.");
        }
    }
}
