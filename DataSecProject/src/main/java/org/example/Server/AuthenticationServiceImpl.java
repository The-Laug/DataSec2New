package org.example.Server;

import org.example.Shared.AuthenticationService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AuthenticationServiceImpl extends UnicastRemoteObject implements AuthenticationService {
    private final Map<String, String> activeChallenges = new ConcurrentHashMap<>();
    private final Map<String, String> activeTokens = new ConcurrentHashMap<>();
    private final Map<String, Long> tokenExpiry = new ConcurrentHashMap<>();
    private final Map<String, List<String>> rolePermissions = new HashMap<>();
    private final Map<String, String> userRoles = new HashMap<>();
    private final Map<String, Set<String>> hierarchy = new HashMap<>();


    private static final long TOKEN_LIFETIME_MS = 300_000; // 5 minutes

    private final PasswdFileManager passwdFileManager;

    protected AuthenticationServiceImpl(PasswdFileManager passwdFileManager) throws IOException {
        this.passwdFileManager = passwdFileManager;
        loadRolesAndHierarchy("roles_policies");
        resolveRoleHierarchy();
        loadUserRoles("user_roles");
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

    public boolean hasPermission(String token, String methodName) throws RemoteException {
        String username = activeTokens.get(token);
        if (username == null) return false;

        String role = userRoles.get(username);
        if (role == null) return false;

        List<String> permissions = rolePermissions.get(role);
        return permissions != null && permissions.contains(methodName);
    }

    private void loadRolesAndHierarchy(String fileName) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        try (BufferedReader reader = new BufferedReader(new FileReader(classLoader.getResource(fileName).getPath()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Role=")) {
                    String[] parts = line.substring(5).split(":");
                    String role = parts[0];
                    String[] permissions = parts[1].split(",");
                    rolePermissions.put(role, new ArrayList<>(Arrays.asList(permissions)));
                } else if (line.startsWith("Hierarchy=")) {
                    String[] parts = line.substring(10).split(":");
                    String role = parts[0];
                    String[] inheritedRoles = parts[1].split(",");
                    hierarchy.put(role, new HashSet<>(Arrays.asList(inheritedRoles)));
                }
            }
        }
    }

    private void loadUserRoles(String fileName) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        try (BufferedReader reader = new BufferedReader(new FileReader(classLoader.getResource(fileName).getPath()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                String username = parts[0];
                String role = parts[1];
                userRoles.put(username, role);
            }
        }
    }

    private void resolveRoleHierarchy() {
        for (String role : hierarchy.keySet()) {
            Set<String> allPermissions = new HashSet<>(rolePermissions.getOrDefault(role, new ArrayList<>()));

            // Recursively add permissions from inherited roles
            for (String inheritedRole : hierarchy.get(role)) {
                allPermissions.addAll(rolePermissions.getOrDefault(inheritedRole, new ArrayList<>()));
            }

            // Update the role with inherited permissions
            rolePermissions.put(role, new ArrayList<>(allPermissions));
        }
    }

    @Override
    public boolean validateAccess(String token, String methodName) throws RemoteException {
        // Step 1: Validate the token
        String username = activeTokens.get(token);
        if (username == null) {
            System.out.println("Invalid or expired token for method: " + methodName);
            return false;
        }

        // Step 2: Get the user's role
        String role = userRoles.get(username);
        if (role == null) {
            System.out.println("User " + username + " does not have a role assigned.");
            return false;
        }

        // Step 3: Check permissions for the role
        if (hasPermissionForRole(role, methodName)) {
            return true;
        } else {
            // System.out.println("User " + username + " with role " + role + " is not allowed to perform: " + methodName);
            return false;
        }
    }

    // Helper method to check if a role has permission, considering the hierarchy
    private boolean hasPermissionForRole(String role, String methodName) {
        // Check direct permissions for the role
        List<String> permissions = rolePermissions.get(role);
        if (permissions != null && permissions.contains(methodName)) {
            return true;
        }

        // Check permissions for inherited roles (recursively)
        Set<String> inheritedRoles = hierarchy.get(role);
        if (inheritedRoles != null) {
            for (String inheritedRole : inheritedRoles) {
                if (hasPermissionForRole(inheritedRole, methodName)) {
                    return true;
                }
            }
        }
        return false;
    }


}
