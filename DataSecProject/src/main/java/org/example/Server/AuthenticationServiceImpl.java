package org.example.Server;

import org.example.Server.PasswdFileManager;
import org.example.Shared.AuthenticationService;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class AuthenticationServiceImpl extends UnicastRemoteObject implements AuthenticationService {
    private final Map<String, String> activeChallenges = new ConcurrentHashMap<>();
    private final Map<String, String> activeTokens = new ConcurrentHashMap<>();
    private final Map<String, Long> tokenExpiry = new ConcurrentHashMap<>();
    private static final long TOKEN_LIFETIME_MS = 300_000; // 5 minutes

    private final PasswdFileManager passwdFileManager;

    protected AuthenticationServiceImpl(PasswdFileManager passwdFileManager) throws RemoteException {
        this.passwdFileManager = passwdFileManager;
    }

    @Override
    public String[] authenticate(String username, String clientResponse) throws Exception {
        if (clientResponse == null) {
            String[] userDetails = passwdFileManager.getUserDetails(username); // [username, salt, hashedPassword]
            String salt = userDetails[1];
            String challenge = generateChallenge();
            activeChallenges.put(username, challenge);
            return new String[]{salt, challenge};
        } else {
            String challenge = activeChallenges.get(username);
            String[] userDetails = passwdFileManager.getUserDetails(username);

            // System.out.println(userDetails[2]);

            String expectedHash = hashPassword(userDetails[2], challenge);

            // System.out.println(clientResponse);
            // System.out.println(expectedHash);

            if (expectedHash.equals(clientResponse)) {
                activeChallenges.remove(username); // Invalidate the challenge
                String token = generateToken(username);
                activeTokens.put(token, username);
                return new String[]{token};
            } else {
                throw new RemoteException("Authentication failed.");
            }
        }
    }


    @Override
    public boolean validateToken(String token) throws RemoteException {
        return tokenExpiry.getOrDefault(token, 0L) > System.currentTimeMillis();
    }

    private String generateChallenge() {
        Random random = new Random();
        return Long.toHexString(random.nextLong());
    }

    private String hashPassword(String storedHashedPassword, String challenge) throws RemoteException {
        try {
            return Base64.getEncoder().encodeToString(
                    MessageDigest.getInstance("SHA-256")
                            .digest((storedHashedPassword + challenge).getBytes())
            );
        } catch (Exception e) {
            throw new RemoteException("Hashing failed.", e);
        }
    }




    private String generateToken(String username) {
        String token = Base64.getEncoder().encodeToString((username + ":" + System.currentTimeMillis()).getBytes());
        activeTokens.put(token, username);
        tokenExpiry.put(token, System.currentTimeMillis() + TOKEN_LIFETIME_MS); // Add token expiry time
        return token;
    }

}
