package org.example.Client;

import org.example.Shared.PrintService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.Naming;
public class ClientAuthenticator {

    private ClientAuthenticator() {
    }

    public static void main(String[] var0) {
        String var1 = "localhost";
/*
        try {
            Registry var2 = LocateRegistry.getRegistry(var1);
            //Naming.rebind("PrintService",var2);
            PrintService var3 = (PrintService) var2.lookup("PrintService");
            String var4 = var3.print();
            System.out.println("response: " + var4);
        } catch (Exception var5) {
            System.err.println("Client exception: " + var5.toString());
            var5.printStackTrace();
        }
        */
        try {
            // Connect to the RMI registry on port 5099
            Registry var2 = LocateRegistry.getRegistry(var1, 5099); // Ensure port matches
            // Look up the PrintService object
            PrintService var3 = (PrintService) var2.lookup("PrintService"); // Look up by name
            String var4 = var3.print(); // Call the remote print method
            System.out.println("response: " + var4);
        } catch (Exception var5) {
            System.err.println("Client exception: " + var5.toString());
            var5.printStackTrace();
        }
    }
}
