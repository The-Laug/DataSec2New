package org.example.Client;

import org.example.Shared.PrintService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
public class ClientAuthenticator {
    private ClientAuthenticator() {
    }
    private String token;

    public static void main(String[] var0) {
        ClientAuthenticator var1 = new ClientAuthenticator();
        var1.authenticate();
    }

    public  void authenticate() {
        String var1 = "localhost";
        try {
            // Connect to the RMI registry on port 5099
            Registry var2 = LocateRegistry.getRegistry(var1, 5099); // Ensure port matches
            // Look up the PrintService object
            PrintService var3 = (PrintService) var2.lookup("PrintService"); // Look up by name

            // Trying to authenticate. First sending empty clientResponse, to get challenge from server
            String[] challengeArr = var3.authenticate("lauge", null);
            //When getting challenge from server, we hash the password with the salt and challenge
            String salt = challengeArr[0];
            String challengeString = challengeArr[1];
            String hashedPassword = hashPassword("password", salt, challengeString);



            // Sending hashed password to server and getting the token
            String[] tokenArr = var3.authenticate("lauge", hashedPassword);
            String token = tokenArr[0];
            this.token = token;
             System.out.println("Token: " + tokenArr[0]);
            // System.out.println("Hashed password with salt and challenge: " + hashedPassword);
        } catch (Exception var5) {
            System.err.println("Client exception: " + var5.toString());
            var5.printStackTrace();
        }
    }

    //Function to hash password with salt and challenge
    public static String hashPassword(String password, String salt, String challenge) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String combined = password + salt;
        byte[] hashed = digest.digest(combined.getBytes());
        // Combine hash with challenge
        digest.reset();
        String challengeCombined = new String(hashed) + challenge;
        byte[] finalHash = digest.digest(challengeCombined.getBytes());
        return bytesToHex(finalHash);
    }


    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
