package org.example.Client;

import org.example.Shared.AuthenticationService;
import org.example.Shared.PrintService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
public class ClientAuthenticator {
    private ClientAuthenticator() {
    }
    private String token;

    public static void main(String[] var0) throws Exception {
        String var1 = "localhost";
        Registry var2 = LocateRegistry.getRegistry(var1, 5099); // Ensure port matches
        PrintService printService = (PrintService) var2.lookup("PrintService"); // Look up by name
        ClientAuthenticator client = new ClientAuthenticator();
        client.authenticate(printService,"lauge","password");

    }

    public  void authenticate(PrintService printService,String username, String password) {
        try {
            String[] challengeArr = AuthenticationService.authenticate(username, null);
            //When getting challenge from server, we hash the password with the salt and challenge
            String salt = challengeArr[0];
            String challengeString = challengeArr[1];
            String hashedPassword = hashPassword(password, salt, challengeString);



            // Sending hashed password to server and getting the token
            String[] tokenArr = printService.authenticate(username, hashedPassword);
            String token = tokenArr[0];
            this.token = token;
             //var3.print("ABC123");
             printService.print("text.txt","Printer.txt" , token);
            // System.out.println("Hashed password with salt and challenge: " + hashedPassword);
        } catch (Exception var5) {
            System.err.println("Client exception: " + var5.toString());
            var5.printStackTrace();
        }
    }

    public static void printHashedPassword(String password, String salt) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String combined = password + salt;
        byte[] hashed = digest.digest(combined.getBytes());
        System.out.println(bytesToHex(hashed));
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
