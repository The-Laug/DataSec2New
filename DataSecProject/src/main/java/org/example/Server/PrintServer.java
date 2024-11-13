package org.example.Server;

import org.example.Shared.PrintService;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;


public class PrintServer implements PrintService {
    public PrintServer() {

    }
    @Override
    public String print() throws RemoteException {
        System.out.println("The printer has printed 2");
        return null;
    }

    public static void main(String[] var0) {
        try {
            PrintServer var1 = new PrintServer();
            PrintService var2 = (PrintService) UnicastRemoteObject.exportObject(var1, 5099);
            Registry var3 = LocateRegistry.createRegistry(5099);
            var3.bind("PrintService", var2);
            System.err.println("Server ready");
        } catch (Exception var4) {
            System.err.println("Server exception: " + var4.toString());
            var4.printStackTrace();
        }

    }
/*
    public static void main(String[] var0) {
        try {
            // Create the server object
            PrintServer var1 = new PrintServer();
            // Export the PrintServer object and register it with the RMI registry
            PrintService var2 = (PrintService) UnicastRemoteObject.exportObject(var1, 0);
            // Create and bind the registry (or use existing registry if it's already running)
            Registry var3 = LocateRegistry.createRegistry(5099); // Custom port
            // Use Naming.rebind to bind the service
            Naming.rebind("PrintService", var2); // Use the same name as the client expects
            System.err.println("Server ready");
        } catch (Exception var4) {
            System.err.println("Server exception: " + var4.toString());
            var4.printStackTrace();
        }
    }
*/

}

