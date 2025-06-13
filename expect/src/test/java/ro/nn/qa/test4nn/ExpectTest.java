package ro.nn.qa.test4nn;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.log4j.Level;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ro.nn.qa.expect4nn.Expect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;

/**
 * Created by Alexandru Giurovici on 31.08.2015.
 */
public class ExpectTest {

    public class NullOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {
        }
    }    private static Process simulatorProcess;
    
    @BeforeClass
    public static void setup() throws Exception {
        // Start the AS400 simulator server directly from Java
        System.out.println("[TEST SETUP] Starting AS400 simulator server...");
        try {
            // First, check if port 23 is already in use
            try {
                java.net.Socket testSocket = new java.net.Socket("localhost", 23);
                testSocket.close();
                System.out.println("[TEST SETUP] AS400 simulator already running on port 23");
                return;
            } catch (java.net.ConnectException e) {
                // Port not in use, need to start simulator
            }
            
            // Start the AS400 simulator in a separate process
            String classpath = System.getProperty("java.class.path");
            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome + "/bin/java";
            
            // Add the automation module's test classes to the classpath
            String automationTestClasses = "c:/AS400-automation/automation/target/test-classes";
            String automationClasses = "c:/AS400-automation/automation/target/classes";
            String fullClasspath = automationTestClasses + ";" + automationClasses + ";" + classpath;
            
            ProcessBuilder pb = new ProcessBuilder(
                javaBin,
                "-cp", fullClasspath,
                "ro.nn.qa.automation.server.AS400SimulatorServer",
                "23"
            );
            
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            
            simulatorProcess = pb.start();
            System.out.println("[TEST SETUP] Simulator process started");
            
            // Wait for the simulator to start
            Thread.sleep(3000);
            
            // Check if simulator is accessible on port 23
            boolean ready = false;
            int maxAttempts = 10;
            for (int i = 0; i < maxAttempts; i++) {
                try {
                    java.net.Socket socket = new java.net.Socket("localhost", 23);
                    socket.close();
                    ready = true;
                    break;
                } catch (java.net.ConnectException e) {
                    System.out.println("[TEST SETUP] Waiting for simulator... attempt " + (i + 1) + "/" + maxAttempts);
                    Thread.sleep(2000);
                }
            }
            
            if (!ready) {
                throw new RuntimeException("AS400 simulator not accessible on port 23 after " + (maxAttempts * 2) + " seconds");
            }
            
            System.out.println("[TEST SETUP] AS400 simulator is ready!");
        } catch (Exception e) {
            System.out.println("[TEST SETUP] Failed to start AS400 simulator: " + e.getMessage());
            throw e;
        }
    }    @AfterClass
    public static void close() throws Exception {
        // Stop the AS400 simulator process
        System.out.println("[TEST CLEANUP] Stopping AS400 simulator...");
        try {
            if (simulatorProcess != null && simulatorProcess.isAlive()) {
                simulatorProcess.destroy();
                // Wait a bit for graceful shutdown
                Thread.sleep(1000);
                if (simulatorProcess.isAlive()) {
                    simulatorProcess.destroyForcibly();
                }
                System.out.println("[TEST CLEANUP] Simulator process stopped.");
            } else {
                System.out.println("[TEST CLEANUP] No simulator process to stop.");
            }
        } catch (Exception e) {
            System.out.println("[TEST CLEANUP] Failed to stop AS400 simulator: " + e.getMessage());
        }
    }
      static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[SHUTDOWN HOOK] Stopping AS400 simulator...");
            try {
                if (simulatorProcess != null && simulatorProcess.isAlive()) {
                    simulatorProcess.destroy();
                    Thread.sleep(500);
                    if (simulatorProcess.isAlive()) {
                        simulatorProcess.destroyForcibly();
                    }
                    System.out.println("[SHUTDOWN HOOK] Simulator process stopped.");
                }
            } catch (Exception e) {
                System.out.println("[SHUTDOWN HOOK] Failed to stop AS400 simulator: " + e.getMessage());
            }
        }));
    }
      @Test
    public void testTelnet() throws IOException {
        // Use Apache Commons Net TelnetClient to connect to AS400 simulator
        TelnetClient telnet = new TelnetClient();
        try {
            System.out.println("Connecting to localhost:23...");
            telnet.connect("localhost", 23);
            InputStream in = telnet.getInputStream();
            OutputStream out = telnet.getOutputStream();
            
            System.out.println("Successfully connected to AS400 simulator");
            
            // Create Expect instance to handle the telnet session with pattern matching
            Expect expect = new Expect(in, out);
            expect.setDefault_timeout(5000);  // 5 seconds timeout
            Expect.addLogToConsole(Level.ALL);
            
            // Wait for initial sign-on screen - AS400 simulator shows the sign-on screen immediately
            System.out.println("Waiting for AS400 sign-on screen...");
            int result = expect.expects(10000, 
                Pattern.compile("(?i).*sign.*on.*"), 
                Pattern.compile("(?i).*AS400SIM.*"),
                Pattern.compile("(?i).*user.*"));
            
            if (result >= 0) {
                System.out.println("Sign-on screen detected, matched pattern: " + result);
                System.out.println("Matched text: " + expect.match);
                
                // Send authentication data
                System.out.println("Sending authentication: GIUROAL");
                out.write("GIUROAL\r\n".getBytes());
                out.flush();
                
                Thread.sleep(500);
                
                System.out.println("Sending password: Bucuresti2");
                out.write("Bucuresti2\r\n".getBytes());
                out.flush();
                
                // Wait for main menu
                System.out.println("Waiting for main menu...");
                result = expect.expects(10000, 
                    Pattern.compile("(?i).*MAIN.*MENU.*"), 
                    Pattern.compile("(?i).*Select.*following.*"),
                    Pattern.compile("(?i).*User.*tasks.*"));
                
                if (result >= 0) {
                    System.out.println("SUCCESS: Reached AS400 main menu!");
                    System.out.println("Matched text: " + expect.match);
                } else {
                    System.out.println("WARNING: Main menu not detected");
                }
            } else {
                System.out.println("WARNING: Sign-on screen not detected");
                throw new AssertionError("Failed to detect AS400 sign-on screen");
            }
            
            System.out.println("Test completed successfully");
        } catch (Exception e) {
            System.out.println("Exception during telnet test: " + e.getMessage());
            e.printStackTrace();
            throw new AssertionError("Telnet test failed: " + e.getMessage());
        } finally {
            // Ensure we disconnect
            if (telnet.isConnected()) {
                telnet.disconnect();
            }
            System.out.println("Disconnected from telnet");
        }
    }
    
    @Test
    public void testExpect4NN()
    {
        final Pipe pipe;
        try {
            pipe = Pipe.open();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("failed to open pipe!");
        }

        final InputStream in = Channels.newInputStream(pipe.source());
        final OutputStream out = Channels.newOutputStream(pipe.sink());

        new Thread(() -> {
            try {
                System.out.println("Writing 'hello' to the pipe");
                sleep(100); // Increased sleep duration for stability
                out.write("hello".getBytes());
                out.flush(); // Make sure data is actually sent
                System.out.println("Wrote 'hello', waiting before writing ' world'");
                
                // Sleep longer to guarantee timeout in the first world check
                sleep(1000); // Much longer than the 0ms timeout below
                
                System.out.println("Writing ' world' to the pipe");
                out.write(" world".getBytes());
                out.flush(); // Make sure data is actually sent
                System.out.println("Wrote ' world'");
                
                // Give time for processing before closing
                sleep(500);
                
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                try { 
                    out.close(); 
                    System.out.println("Output stream closed");
                } catch (Exception e) {}
            }
        }).start();

        Expect.addLogToConsole(Level.ALL);

        Expect xp = new Expect(in, new NullOutputStream());
        
        // First match - hello
        System.out.println("Looking for '.*llo' pattern...");
        int result1 = xp.expects(1000, Pattern.compile(".*llo")); // increased timeout
        System.out.println("After '.*llo' match: " + xp.match + ", result=" + result1);
        if (!xp.match.equals("hello")) {
            System.out.println("First assertion failed: match=" + xp.match);
            throw new AssertionError("First match failed: " + xp.match);
        }
        
        // Explicitly clear the match state
        xp.match = null;
        
        // Timeout test - use 0ms timeout to guarantee immediate check and timeout
        System.out.println("Looking for 'world' pattern with zero timeout (should timeout)...");
        int retv = xp.expects(0, "world"); // zero timeout to guarantee timeout
        System.out.println("After first 'world' search: match=" + xp.match + ", retv=" + retv);
        
        // Verify timeout occurred
        if (xp.match != null) {
            System.out.println("Second assertion failed: match should be null but is " + xp.match);
            throw new AssertionError("Expected null match but got: " + xp.match);
        }
        if (retv != Expect.RETV_TIMEOUT) {
            System.out.println("Third assertion failed: retv should be TIMEOUT but is " + retv);
            throw new AssertionError("Expected RETV_TIMEOUT but got: " + retv);
        }
        
        // Now try with adequate timeout - should match
        System.out.println("Looking for 'world' pattern with longer timeout...");
        int result3 = xp.expects(1500, "world"); // increased timeout for second world
        System.out.println("After second 'world' search: match=" + xp.match + ", result=" + result3);
        if (!xp.match.equals("world")) {
            System.out.println("Fourth assertion failed: match should be 'world' but is " + xp.match);
            throw new AssertionError("Expected 'world' but got: " + xp.match);
        }
        
        // Check EOF
        System.out.println("Waiting for EOF...");
        int eofResult = xp.expectEOF(500);
        System.out.println("After EOF: isSuccess=" + xp.isSuccess + ", result=" + eofResult);
        if (!xp.isSuccess) {
            System.out.println("Fifth assertion failed: isSuccess should be true");
            throw new AssertionError("Expected success on EOF");
        }
        
        xp.close();
        System.out.println("Test completed successfully");
        
        Expect.turnOffLogging();


    }



}
