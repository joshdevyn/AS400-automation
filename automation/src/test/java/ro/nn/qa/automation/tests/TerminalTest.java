package ro.nn.qa.automation.tests;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.nn.qa.automation.terminal.AS400Terminal;
import ro.nn.qa.automation.terminal.AS400Screen;
import ro.nn.qa.automation.terminal.AS400Field;
import ro.nn.qa.bootstrap.Controller;

import java.util.List;

import static java.lang.Thread.sleep;

/**
 * Terminal Test - Modernized to use AS400Terminal instead of TN5250j
 */
public class TerminalTest {
    private static final Logger logger = LoggerFactory.getLogger(TerminalTest.class);
    
    protected Controller controller;
    protected AS400Terminal terminal;    @Before
    public void start() {
        controller = Controller.getInstance();
        if (!controller.isAlive()) {
            controller.start();
        }
    }

    @Test
    public void myTerminalTest() throws Exception {
        logger.info("Starting terminal test");
        
        // Create AS400Terminal with connection parameters
        terminal = new AS400Terminal("localhost", 23, "GIUROAL", "Bucuresti1", AS400Terminal.ConnectionType.TELNET);

        // Connect to the AS400 system
        terminal.connect();
        logger.info("Connected to AS400");        // Wait for initial screen
        sleep(5000);
          // Get current screen
        AS400Screen screen = terminal.getScreen();
        List<AS400Field> fields = screen.getFields();
        
        if (fields.size() >= 2) {
            // Fill in username
            AS400Field userName = fields.get(0);
            if (userName.isInputField()) {
                userName.setValue("GIUROAL");
                logger.info("Entered username");
            }

            // Fill in password
            AS400Field password = fields.get(1);
            if (password.isInputField()) {
                password.setValue("Bucuresti1");
                logger.info("Entered password");
            }
        }

        // Press Enter to submit login
        terminal.pressEnter();
        sleep(1000);
        
        // Press Enter again (common AS400 login flow)
        terminal.pressEnter();

        // Enter environment selection (72)
        terminal.sendText("72");
        terminal.pressEnter();
        sleep(1000);

        // Navigate through menu system
        logger.info("Navigating through menus");
        
        // Press F3 to exit
        terminal.sendFunctionKey(3);
        sleep(1000);
        
        // Press F3 again to fully exit
        terminal.sendFunctionKey(3);
        
        // Disconnect
        terminal.disconnect();
        logger.info("Terminal test completed successfully");
    }
}
