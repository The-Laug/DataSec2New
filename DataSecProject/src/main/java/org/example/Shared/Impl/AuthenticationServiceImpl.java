package org.example.Shared.Impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.example.Client.BinAscii;
import org.example.Server.PasswdFileManager;
import org.example.Shared.AuthenticationService;

import java.io.IOException;
import java.rmi.RemoteException;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;


public class AuthenticationServiceImpl implements AuthenticationService {

    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256); // Generate a secure random key
    private final Map<String, String> activeChallenges = new ConcurrentHashMap<>(); // username -> challenge
    private final Map<String, String> activeTokens = new ConcurrentHashMap<>(); // token -> username
    private final Map<String, Long> tokenExpiry = new ConcurrentHashMap<>(); // token -> expiry time
    private static final long TOKEN_LIFETIME_MS = 5*60*1000; // 5 minutes = 5 * 60 * 1000

    private PasswdFileManager passwdFileManager = new PasswdFileManager("src/main/java/org/example/resources/passwd");

    @Override
    public String[] authenticate(String username, String clientResponse) throws RemoteException {
        System.out.println("User: " + username +  " is trying to connect to server");
        if (clientResponse == null) {
            System.out.println("No active session");
            try {
                String[] challenge = passwdFileManager.getUserDetails(username);
                String challengeString = getChallengeString();
                activeChallenges.put(username, challengeString);
                String[] challengeArr = new String[]{challenge[1], challengeString};
                System.out.println("Challenge is sent to user");
                return challengeArr;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("Active session");
            try {
                String[] userinfo = passwdFileManager.getUserDetails(username);
                String salt = userinfo[1];
                String challengeString = activeChallenges.get(username);
                String password = userinfo[2];
                if (verifyPassword( salt, challengeString, clientResponse, password)) {
                    System.out.println("User is authenticated");


                    // Generate and store token
                    String token = generateToken(username);
                    activeTokens.put(token, username);
                    tokenExpiry.put(token, System.currentTimeMillis() + TOKEN_LIFETIME_MS);
                    String[] tokenArr = new String[]{token};
                    return tokenArr;
                }
                else {
                    System.out.println("User is not authenticated - Wrong Password");
                    throw new RemoteException("User is not authenticated");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    //Auxiliary function to convert byte[] to hex
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    protected String getChallengeString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }

    //Function to challenge the password sent by the user
    public static String challengePassword(String hashed, String salt, String challenge) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        // Combine hash with challenge
        byte[] byteHashed = BinAscii.unhexlify(hashed);

        // Very important, took some time - need hash in byte[] type
        String challengeCombined = new String(byteHashed) + challenge;
        byte[] finalHash = digest.digest(challengeCombined.getBytes());
        return bytesToHex(finalHash);
    }

    public static boolean verifyPassword( String salt, String challenge, String clientResponse, String password) throws Exception {
        String serverResponse = challengePassword(password, salt, challenge);
        System.out.println("Server response: " + serverResponse);
        System.out.println("Client response: " + clientResponse);
        return serverResponse.equals(clientResponse);
    }

    public String generateToken(String username) {
        long now = System.currentTimeMillis();
        long expiryTime = TOKEN_LIFETIME_MS + now;
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(expiryTime))
                .signWith(key)
                .compact();
    }

    // Validate a JWT
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            System.err.println("Invalid or expired JWT: " + e.getMessage());
            return false;
        }
    }

    // Extract username from a JWT
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}
