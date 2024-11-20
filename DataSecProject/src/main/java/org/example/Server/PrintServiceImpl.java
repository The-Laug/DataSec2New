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
        if (!validateAccess(token, "print")) return; // Access denied, exit method
        printerQueues.putIfAbsent(printer, new ArrayList<>());
        printerQueues.get(printer).add(filename);
        System.out.println("Printed file " + filename + " from printer " + printer);
    }

    @Override
    public List<String> queue(String token, String printer) throws RemoteException {
        if (!validateAccess(token, "queue")) return Collections.emptyList(); // Access denied, return empty list
        System.out.println("Queue for printer " + printer + ": " + printerQueues.getOrDefault(printer, Collections.emptyList()));
        return printerQueues.getOrDefault(printer, Collections.emptyList());
    }

    @Override
    public void topQueue(String token, String printer, int job) throws RemoteException {
        if (!validateAccess(token, "topQueue")) return; // Access denied, exit method
        System.out.println("Moving job " + job + " to the top of the queue for printer " + printer);
        List<String> queue = printerQueues.get(printer);
        if (queue != null && job >= 0 && job < queue.size()) {
            String jobFile = queue.remove(job);
            queue.add(0, jobFile);
        }
    }

    @Override
    public void stop(String token) throws RemoteException {
        if (!validateAccess(token, "stop")) return; // Access denied, exit method
        System.out.println("Printer stopped.");
    }

    @Override
    public void start(String token) throws RemoteException {
        if (!validateAccess(token, "start")) return; // Access denied, exit method
        System.out.println("Printer started.");
    }

    @Override
    public void restart(String token) throws RemoteException {
        if (!validateAccess(token, "restart")) return; // Access denied, exit method
        System.out.println("Printer restarted.");
    }

    @Override
    public void status(String token, String printer) throws RemoteException {
        if (!validateAccess(token, "status")) return; // Access denied, exit method
        System.out.println("Printer status: OK");
    }

    @Override
    public void readConfig(String token, String parameter) throws RemoteException {
        if (!validateAccess(token, "readConfig")) return; // Access denied, exit method
        System.out.println("Read config: " + parameter);
    }

    @Override
    public void setConfig(String token, String parameter, String value) throws RemoteException {
        if (!validateAccess(token, "setConfig")) return; // Access denied, exit method
        System.out.println("Set config: " + parameter + " = " + value);
    }

    private boolean validateAccess(String token, String methodName) throws RemoteException {
        boolean hasAccess = authService.validateAccess(token, methodName);
        if (!hasAccess) {
            System.out.println("Access Denied: You do not have permission to " + methodName);
        }
        return hasAccess;
    }
}
