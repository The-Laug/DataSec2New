package org.example.Client;

import org.example.Shared.AuthenticationService;
import org.example.Shared.PrintService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientAuthenticator {
    public static void main(String[] args) {
        try {
            // Connect to the authentication service
            Registry registry = LocateRegistry.getRegistry("localhost", 5099); // Port for Authentication Service
            AuthenticationService authService = (AuthenticationService) registry.lookup("AuthenticationService");

            // Begin authentication
            String[] challengeResponse = authService.authenticate("lauge", null); // Request challenge
            String salt = challengeResponse[0];
            String challenge = challengeResponse[1];

            // Hash the password
            String hashedPassword = hashPassword("password", salt, challenge);

            // Authenticate and get the token
            String[] tokenResponse = authService.authenticate("lauge", hashedPassword);
            String token = tokenResponse[0];

            System.out.println("Token received: " + token);

            // Connect to the print service
            PrintService printService = (PrintService) registry.lookup("PrintService");

            // Example operations
            printService.print(token, "file.txt", "printer1");
            printService.queue(token, "printer1");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String hashPassword(String password, String salt, String challenge) throws Exception {
        return org.example.Client.BinAscii.hexlify(
                java.security.MessageDigest.getInstance("SHA-256")
                        .digest((password + salt + challenge).getBytes())
        );
    }
}
