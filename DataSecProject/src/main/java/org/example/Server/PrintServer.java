package org.example.Server;

import org.example.Shared.AuthenticationService;
import org.example.Shared.PrintService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class PrintServer {
    public static void main(String[] args) {
        try {
            // Specify the resource path relative to src/main/resources
            String resourcePath = "passwd";

            PasswdFileManager passwdFileManager = new PasswdFileManager(resourcePath);

            AuthenticationService authService = new AuthenticationServiceImpl(passwdFileManager);
            PrintService printService = new PrintServiceImpl(authService);

            Registry registry = LocateRegistry.createRegistry(5099);

            registry.bind("AuthenticationService", authService);
            registry.bind("PrintService", printService);

            System.out.println("Server is running.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
