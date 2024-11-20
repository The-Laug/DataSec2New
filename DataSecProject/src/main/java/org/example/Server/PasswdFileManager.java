package org.example.Server;

import java.io.*;
import java.net.URL;
import java.util.*;

public class PasswdFileManager {
    private final String passwdFilePath;

    public PasswdFileManager(String resourcePath) {
        // Use ClassLoader to locate the resource file
        URL resourceUrl = getClass().getClassLoader().getResource(resourcePath);
        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource file not found: " + resourcePath);
        }
        this.passwdFilePath = resourceUrl.getPath();
    }

    // Add a user to the passwd file
    public void addUser(String username, String salt, String hashedPassword) throws IOException {
        if (userExists(username)) {
            throw new IllegalArgumentException("User already exists.");
        }

        try (FileWriter fw = new FileWriter(passwdFilePath, true)) {
            fw.write(username + ":" + salt + ":" + hashedPassword + "\n");
        }
    }

    // Remove a user from the passwd file
    public void removeUser(String username) throws IOException {
        List<String> lines = new ArrayList<>();
        boolean userFound = false;

        try (BufferedReader br = new BufferedReader(new FileReader(passwdFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith(username + ":")) {
                    lines.add(line);
                } else {
                    userFound = true;
                }
            }
        }

        if (!userFound) {
            throw new IllegalArgumentException("User not found.");
        }

        try (FileWriter fw = new FileWriter(passwdFilePath)) {
            for (String line : lines) {
                fw.write(line + "\n");
            }
        }
    }

    // Modify a user's hashed password
    public void modifyUserPassword(String username, String newSalt, String newHashedPassword) throws IOException {
        List<String> lines = new ArrayList<>();
        boolean userFound = false;

        try (BufferedReader br = new BufferedReader(new FileReader(passwdFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(username + ":")) {
                    lines.add(username + ":" + newSalt + ":" + newHashedPassword);
                    userFound = true;
                } else {
                    lines.add(line);
                }
            }
        }

        if (!userFound) {
            throw new IllegalArgumentException("User not found.");
        }

        try (FileWriter fw = new FileWriter(passwdFilePath)) {
            for (String line : lines) {
                fw.write(line + "\n");
            }
        }
    }

    // Check if a user exists in the passwd file
    public boolean userExists(String username) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(passwdFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(username + ":")) {
                    return true;
                }
            }
        }
        return false;
    }

    // Retrieve user details
    public String[] getUserDetails(String username) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(passwdFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(username + ":")) {
                    return line.split(":");
                }
            }
        }
        throw new IllegalArgumentException("User not found.");
    }
}
