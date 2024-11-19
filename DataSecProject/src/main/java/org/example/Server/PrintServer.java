package org.example.Server;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.example.Client.BinAscii;
import org.example.Shared.PrintService;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.Key;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class PrintServer implements Remote {
    public PrintServer() {
    }


    public static void main(String[] var0) {
        try {
            PrintServer var1 = new PrintServer();
            PrintService var2 = (PrintService) UnicastRemoteObject.exportObject(var1, 5099);
            Registry var3 = LocateRegistry.createRegistry(5099);
            var3.bind("PrintService", var2);
            System.err.println("Server ready");
        } catch (Exception var4) {
            System.err.println("Server exception: " + var4.toString());
            var4.printStackTrace();
        }
    }




}

