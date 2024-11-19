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
    public String[] authenticate(String username, String clientResponse) throws IOException{
        if (clientResponse == null) {
            String salt = passwdFileManager.getUserDetails(username)[1];
            String challenge = generateChallenge();
            activeChallenges.put(username, challenge);
            return new String[]{salt, challenge};
        } else {
            String[] userDetails = passwdFileManager.getUserDetails(username);
            String expectedHash = hashPassword(userDetails[2], userDetails[1], activeChallenges.get(username));
            if (expectedHash.equals(clientResponse)) {
                activeChallenges.remove(username);
                String token = generateToken(username);
                activeTokens.put(token, username);
                tokenExpiry.put(token, System.currentTimeMillis() + TOKEN_LIFETIME_MS);
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

    private String hashPassword(String password, String salt, String challenge) throws RemoteException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String combined = password + salt + challenge;
            return Base64.getEncoder().encodeToString(digest.digest(combined.getBytes()));
        } catch (Exception e) {
            throw new RemoteException("Hashing failed.", e);
        }
    }

    private String generateToken(String username) {
        return Base64.getEncoder().encodeToString((username + ":" + System.currentTimeMillis()).getBytes());
    }
}
