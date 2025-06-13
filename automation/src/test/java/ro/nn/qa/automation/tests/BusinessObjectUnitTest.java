package ro.nn.qa.automation.tests;

import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import static org.junit.Assert.*;

import ro.nn.qa.automation.terminal.MockAS400Terminal;
import ro.nn.qa.automation.terminal.AS400Terminal;
import ro.nn.qa.bootstrap.Controller;
import ro.nn.qa.business.*;

/**
 * Unit tests for AS400 business objects
 */
public class BusinessObjectUnitTest {
    
    private MockAS400Terminal mockTerminal;
    private AS400Terminal terminal;
    private Controller controller;    @Before
    public void setUp() throws Exception {
        // Initialize mock terminal for testing
        mockTerminal = new MockAS400Terminal();
        controller = Controller.getInstance();
        if (!controller.isAlive()) {
            controller.start();
        }
        // Initialize AS400Terminal with test parameters
        terminal = new AS400Terminal("localhost", 23, "testuser", "testpass", AS400Terminal.ConnectionType.TELNET);
        
        // Connect mock terminal
        assertTrue("Mock terminal should connect", mockTerminal.connect("localhost", 23));
        assertTrue("Mock login should succeed", 
                  mockTerminal.login("GIUROAL", "Bucuresti2", "Bucuresti2"));
    }
    
    @After
    public void tearDown() throws Exception {
        if (mockTerminal != null) {
            mockTerminal.disconnect();
        }
        if (controller != null) {
            // Use interrupt instead of deprecated stop()
            controller.interrupt();
        }
    }
    
    @Test
    public void testTerminalConnection() {
        assertTrue("Terminal should be connected", mockTerminal.isConnected());
        assertEquals("Should be on main menu", "MAIN_MENU", mockTerminal.getCurrentScreen());
    }
    
    @Test
    public void testBusinessObjectCreation() throws Exception {
        BusinessObjectX businessObject = new BusinessObjectX(terminal);
        assertNotNull("Business object should be created", businessObject);
    }
    
    @Test
    public void testMainMenuNavigation() throws Exception {
        mockTerminal.navigateToMenu("new business");
        assertEquals("Should navigate to new business", "NEW_BUSINESS", mockTerminal.getCurrentScreen());
        
        mockTerminal.navigateToMenu("client administration");
        assertEquals("Should navigate to client admin", "CLIENT_ADMIN", mockTerminal.getCurrentScreen());
    }
    
    @Test
    public void testContractCreationFlow() throws Exception {
        // Test the contract creation workflow
        mockTerminal.navigateToMenu("new contract");
        assertEquals("Should be on contract screen", "NEW_CONTRACT", mockTerminal.getCurrentScreen());
        
        // Simulate contract data entry
        mockTerminal.setField(1, "1R1"); // Contract type
        mockTerminal.setField(2, "John Doe"); // Owner
        mockTerminal.setField(3, "01/01/2025"); // Date
          // Verify data was set
        assertEquals("Contract type should be set", "1R1", mockTerminal.getField(1));
    }
    
    @Test
    public void testDateValidation() {
        // Test valid dates
        assertTrue("Valid date should pass", mockTerminal.validateDate("01/01/2025"));
        assertTrue("Valid date should pass", mockTerminal.validateDate("12/31/2025"));
        
        // Test invalid dates
        assertFalse("Invalid date should fail", mockTerminal.validateDate("32/13/2025"));
        assertFalse("Invalid format should fail", mockTerminal.validateDate("2025-01-01"));
        assertFalse("Invalid format should fail", mockTerminal.validateDate("01/01/25"));
    }
    
    @Test
    public void testAmountValidation() {
        // Test valid amounts
        assertTrue("Valid amount should pass", mockTerminal.validateAmount("1000"));
        assertTrue("Valid amount should pass", mockTerminal.validateAmount("10000.50"));
        
        // Test invalid amounts
        assertFalse("Below minimum should fail", mockTerminal.validateAmount("50"));
        assertFalse("Invalid format should fail", mockTerminal.validateAmount("abc"));
        assertFalse("Negative amount should fail", mockTerminal.validateAmount("-100"));
    }
    
    @Test
    public void testScreenNavigation() {
        // Test forward navigation
        mockTerminal.sendKeys("[enter]");
        assertEquals("Should advance screen", "NEW_BUSINESS", mockTerminal.getCurrentScreen());
        
        // Test backward navigation
        mockTerminal.sendKeys("[pf3]");
        assertEquals("Should go back", "MAIN_MENU", mockTerminal.getCurrentScreen());
    }
    
    @Test
    public void testF4HelpFunctionality() {
        mockTerminal.navigateToMenu("new contract");
        
        // Test F4 help activation
        mockTerminal.sendKeys("[pf4]");
        // F4 help should be activated (logged in mock)
        assertEquals("Should still be on contract screen", "NEW_CONTRACT", mockTerminal.getCurrentScreen());
    }
    
    @Test
    public void testSessionCleanup() {
        assertTrue("Session should be active", mockTerminal.isConnected());
        
        mockTerminal.closeSession();
        assertFalse("Session should be closed", mockTerminal.isConnected());
    }
    
    @Test
    public void testErrorHandling() {
        // Test invalid login
        MockAS400Terminal testTerminal = new MockAS400Terminal();
        testTerminal.connect("localhost", 23);
        
        assertFalse("Invalid login should fail", 
                   testTerminal.login("INVALID", "WRONG", "TEST"));
        assertNotEquals("Should not reach main menu", "MAIN_MENU", testTerminal.getCurrentScreen());
    }
}
