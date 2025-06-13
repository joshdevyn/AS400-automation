package ro.nn.qa.automation.tests;

import java.io.*;
import java.net.Socket;

/**
 * Simple test client to verify the AS400 simulator server is working
 */
public class SimpleSocketTest {
    
    public static void main(String[] args) {
        try {
            System.out.println("Connecting to localhost:23...");
            Socket socket = new Socket("localhost", 23);
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            
            System.out.println("Connected! Reading initial data...");
            
            // Read initial screen
            StringBuilder screen = new StringBuilder();
            char[] buffer = new char[1024];
            int bytesRead = reader.read(buffer);
            if (bytesRead > 0) {
                screen.append(buffer, 0, bytesRead);
                System.out.println("Received data:");
                System.out.println("==============");
                System.out.println(screen.toString());
                System.out.println("==============");
            } else {
                System.out.println("No data received");
            }
            
            // Send some test data
            System.out.println("Sending test input: GIUROAL");
            writer.println("GIUROAL");
            
            // Read response
            Thread.sleep(1000);
            if (reader.ready()) {
                bytesRead = reader.read(buffer);
                if (bytesRead > 0) {
                    System.out.println("Response:");
                    System.out.println("=========");
                    System.out.println(new String(buffer, 0, bytesRead));
                    System.out.println("=========");
                }
            }
            
            socket.close();
            System.out.println("Test completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
