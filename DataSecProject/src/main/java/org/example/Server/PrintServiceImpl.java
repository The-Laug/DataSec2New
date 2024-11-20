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
        if (authService.hasPermission(token, "print")){
            System.out.println("Permission denied.");
            printerQueues.putIfAbsent(printer, new ArrayList<>());
            printerQueues.get(printer).add(filename);
            System.out.println("Printed file " + filename + " from printer " + printer);
        } else {
            System.out.println("Permission denied.");
        }
    }

    @Override
    public List<String> queue(String token, String printer) throws RemoteException {
        validateToken(token);
        if (!authService.hasPermission(token, "queue")) {
            System.out.println("Permission denied.");
            return Collections.emptyList();
        }
        System.out.println("Queue for printer " + printer + ": " + printerQueues.getOrDefault(printer, Collections.emptyList()));
        return printerQueues.getOrDefault(printer, Collections.emptyList());
    }

    @Override
    public void topQueue(String token, String printer, int job) throws RemoteException {
        validateToken(token);
        if (!authService.hasPermission(token, "topQueue")) {
            System.out.println("Permission denied.");
            return;
        }
        System.out.println("Moving job " + job + " to the top of the queue for printer " + printer);
        List<String> queue = printerQueues.get(printer);
        if (queue != null && job >= 0 && job < queue.size()) {
            String jobFile = queue.remove(job);
            queue.add(0, jobFile);
        }
    }

    @Override
    public void stop(String token) throws RemoteException {
        validateToken(token);
        if (!authService.hasPermission(token, "stop")) {
            System.out.println("Permission denied.");
            return;
        }
        System.out.println("Printer stopped.");
    }

    @Override
    public void start(String token) throws RemoteException {
        validateToken(token);
        if (!authService.hasPermission(token, "start")) {
            System.out.println("Permission denied.");
            return;
        }
        System.out.println("Printer started.");
    }

    @Override
    public void restart(String token) throws RemoteException {
        validateToken(token);
        if (!authService.hasPermission(token, "restart")) {
            System.out.println("Permission denied.");
            return;
        }
        System.out.println("Printer restarted.");
    }

    @Override
    public void status(String token, String printer) throws RemoteException {
        validateToken(token);
        if (!authService.hasPermission(token, "status")) {
            System.out.println("Permission denied.");
            return;
        }
        System.out.println("Printer status: OK");
    }

    @Override
    public void readConfig(String token, String parameter) throws RemoteException {
        validateToken(token);
        if (!authService.hasPermission(token, "readConfig")) {
            System.out.println("Permission denied.");
            return;
        }
        System.out.println("Read config: " + parameter);
    }

    @Override
    public void setConfig(String token, String parameter, String value) throws RemoteException {
        validateToken(token);
        if (!authService.hasPermission(token, "setConfig")) {
            System.out.println("Permission denied.");
            return;
        }
        System.out.println("Set config: " + parameter + " = " + value);
    }

    private void validateToken(String token) throws RemoteException {
        if (!authService.validateToken(token)) {
            throw new RemoteException("Invalid or expired token.");
        }
    }
}
