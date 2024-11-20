package org.example.Client;

import org.example.Shared.AuthenticationService;
import org.example.Shared.PrintService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.util.Base64;

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
            System.out.println(hashedPassword);

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
        // Step 1: Hash plaintext password with salt
        byte[] firstHash = MessageDigest.getInstance("SHA-256")
                .digest((password + salt).getBytes());

        // Step 2: Hash intermediate result with challenge
        byte[] finalHash = MessageDigest.getInstance("SHA-256")
                .digest((Base64.getEncoder().encodeToString(firstHash) + challenge).getBytes());

        return Base64.getEncoder().encodeToString(finalHash);
    }



}
