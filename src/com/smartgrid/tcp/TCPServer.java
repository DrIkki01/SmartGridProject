package com.smartgrid.tcp;

import com.smartgrid.model.EnergyRecord;
import java.io.*;
import java.net.*;
import java.util.*;

public class TCPServer {
    private static final int PORT = 8888;
    
    private ServerSocket serverSocket;
    private List<EnergyRecord> receivedRecords;
    
    public TCPServer() {
        receivedRecords = new ArrayList<>();
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("TCP Server started on port " + PORT);
            System.out.println("Waiting for batch transfer...");
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }
    
    public void start() {
        try {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress());
            
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            
            int totalRecords = in.readInt();
            System.out.println("Expecting " + totalRecords + " records...");
            
            for (int i = 0; i < totalRecords; i++) {
                EnergyRecord record = (EnergyRecord) in.readObject();
                receivedRecords.add(record);
                
                if ((i + 1) % 1000 == 0 || (i + 1) == totalRecords) {
                    System.out.println("Received " + (i + 1) + "/" + totalRecords + " records...");
                }
            }
            
            // Read end marker
            String endMarker = (String) in.readObject();
            if (endMarker.equals("END_OF_BATCH")) {
                System.out.println("\nBatch transfer complete! " + receivedRecords.size() + " records received.");
            }
            
            in.close();
            clientSocket.close();
            
            if (!receivedRecords.isEmpty()) {
                TCPAnalysis analyzer = new TCPAnalysis();
                analyzer.analyze(receivedRecords);
            } else {
                System.err.println("No records received!");
            }
            
        } catch (EOFException e) {
            System.err.println("Connection closed unexpectedly. Received " + receivedRecords.size() + " records.");
        } catch (Exception e) {
            System.err.println("Error receiving batch: " + e.getMessage());
            e.printStackTrace();
        } finally {
            close();
        }
    }
    
    private void close() {
        try {
            if (serverSocket != null) serverSocket.close();
            System.out.println("TCP Server closed.");
        } catch (IOException e) {
            System.err.println("Error closing: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        TCPServer server = new TCPServer();
        server.start();
    }
}