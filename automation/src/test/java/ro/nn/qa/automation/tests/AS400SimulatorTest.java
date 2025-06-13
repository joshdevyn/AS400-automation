package ro.nn.qa.automation.tests;

import org.junit.Test;
import ro.nn.qa.automation.terminal.AS400Terminal;
import ro.nn.qa.automation.terminal.AS400Screen;

/**
 * Test AS400Terminal against the local AS400 simulator
 */
public class AS400SimulatorTest {
    
    @Test
    public void testAS400SimulatorConnection() throws Exception {
        System.out.println("Testing AS400Terminal against local simulator...");
        
        // Create terminal connection to localhost simulator
        AS400Terminal terminal = new AS400Terminal(
            "localhost", 
            23, 
            "GIUROAL", 
            "Bucuresti2", 
            AS400Terminal.ConnectionType.TELNET
        );
        
        try {
            // Connect to simulator
            System.out.println("Connecting to AS400 simulator...");
            terminal.connect();
            System.out.println("Connected successfully!");
            
            // Wait for initial screen
            Thread.sleep(2000);
            
            // Get current screen
            String screen = terminal.getCurrentScreen();
            System.out.println("Current screen content:");
            System.out.println("========================");
            System.out.println(screen);
            System.out.println("========================");
            
            // Test login
            System.out.println("\nTesting login...");
            boolean loginResult = terminal.login("GIUROAL", "Bucuresti2", "");
            System.out.println("Login result: " + loginResult);
            
            // Wait for response
            Thread.sleep(2000);
            
            // Get main menu screen
            screen = terminal.getCurrentScreen();
            System.out.println("\nMain menu screen:");
            System.out.println("=================");
            System.out.println(screen);
            System.out.println("=================");
            
            // Test navigation to business menu
            System.out.println("\nNavigating to business menu...");
            terminal.sendText("1");
            terminal.sendKey("ENTER");
            Thread.sleep(2000);
            
            screen = terminal.getCurrentScreen();
            System.out.println("\nBusiness menu screen:");
            System.out.println("====================");
            System.out.println(screen);
            System.out.println("====================");
            
            // Test AS400Screen object
            AS400Screen as400Screen = terminal.getScreen();
            System.out.println("\nScreen object created: " + (as400Screen != null));
            
            System.out.println("\n✅ AS400 Simulator test completed successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            // Disconnect
            terminal.disconnect();
            System.out.println("Disconnected from simulator.");
        }
    }
}
