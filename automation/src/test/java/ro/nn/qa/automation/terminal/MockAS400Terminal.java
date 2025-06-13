package ro.nn.qa.automation.terminal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock AS400 terminal for testing purposes
 * This simulates AS400 screens and responses without requiring an actual AS400 connection
 */
public class MockAS400Terminal {
    
    private static final Logger logger = LoggerFactory.getLogger(MockAS400Terminal.class);
    private String currentScreen = "SIGNON";
    private Map<String, String> screenData = new HashMap<>();
    private Map<Integer, String> fieldData = new HashMap<>();
    private boolean connected = false;
    private String currentUser = "";
    
    public MockAS400Terminal() {
        initializeScreens();
    }
    
    private void initializeScreens() {
        // Initialize mock screen data
        screenData.put("SIGNON", "AS/400 Sign On Screen");
        screenData.put("MAIN_MENU", "Main Menu - Select Option");
        screenData.put("NEW_BUSINESS", "New Business Menu");
        screenData.put("NEW_CONTRACT", "New Contract Proposal");
        screenData.put("ENDOWMENT_1", "New Endowment Entry - Page 1");
        screenData.put("ENDOWMENT_2", "New Endowment Entry - Page 2");
        screenData.put("CLIENT_ADMIN", "Client Administration");
        logger.info("Mock AS400 terminal initialized with screens: {}", screenData.keySet());
    }
    
    public boolean connect(String host, int port) {
        logger.info("Mock connection to AS400 host: {}:{}", host, port);
        connected = true;
        currentScreen = "SIGNON";
        return true;
    }
    
    public boolean login(String username, String password, String environment) {
        logger.info("Mock login attempt: {} / {}", username, environment);
        
        // Simulate authentication
        if ("GIUROAL".equals(username) && "Bucuresti2".equals(password)) {
            currentUser = username;
            currentScreen = "MAIN_MENU";
            logger.info("Mock login successful");
            return true;
        } else {
            logger.warn("Mock login failed - invalid credentials");
            return false;
        }
    }
    
    public void sendKeys(String keys) {
        logger.info("Mock sendKeys: {}", keys);
        
        // Simulate screen navigation based on key input
        if (keys.contains("[enter]")) {
            handleEnterKey();
        } else if (keys.contains("[pf3]")) {
            handleF3Key();
        } else if (keys.contains("[pf4]")) {
            handleF4Key();
        } else if (keys.contains("[tab]")) {
            handleTabKey();
        }
    }
    
    private void handleEnterKey() {
        // Simulate screen transitions on Enter
        switch (currentScreen) {
            case "SIGNON":
                currentScreen = "MAIN_MENU";
                break;
            case "MAIN_MENU":
                currentScreen = "NEW_BUSINESS";
                break;
            case "NEW_BUSINESS":
                currentScreen = "NEW_CONTRACT";
                break;
            case "NEW_CONTRACT":
                currentScreen = "ENDOWMENT_1";
                break;
            case "ENDOWMENT_1":
                currentScreen = "ENDOWMENT_2";
                break;
        }
        logger.info("Mock screen transition to: {}", currentScreen);
    }
    
    private void handleF3Key() {
        // Simulate back navigation
        switch (currentScreen) {
            case "ENDOWMENT_2":
                currentScreen = "ENDOWMENT_1";
                break;
            case "ENDOWMENT_1":
                currentScreen = "NEW_CONTRACT";
                break;
            case "NEW_CONTRACT":
                currentScreen = "NEW_BUSINESS";
                break;
            case "NEW_BUSINESS":
                currentScreen = "MAIN_MENU";
                break;
            case "CLIENT_ADMIN":
                currentScreen = "MAIN_MENU";
                break;
        }
        logger.info("Mock F3 navigation to: {}", currentScreen);
    }
    
    private void handleF4Key() {
        logger.info("Mock F4 help activated on screen: {}", currentScreen);
        // F4 help would show context-sensitive help
    }
    
    private void handleTabKey() {
        logger.info("Mock tab navigation on screen: {}", currentScreen);
        // Tab would move between fields
    }
    
    public String getCurrentScreenText() {
        return screenData.getOrDefault(currentScreen, "Unknown Screen");
    }
    
    public String getCurrentScreen() {
        return currentScreen;
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public void disconnect() {
        connected = false;
        currentScreen = "DISCONNECTED";
        currentUser = "";
        logger.info("Mock AS400 terminal disconnected");
    }
    
    public void closeSession() {
        disconnect();
    }
    
    // Mock field operations
    public void setField(int fieldIndex, String value) {
        logger.info("Mock setField[{}] = {}", fieldIndex, value);
        fieldData.put(fieldIndex, value);
    }
    
    public String getField(int fieldIndex) {
        logger.info("Mock getField[{}]", fieldIndex);
        return fieldData.getOrDefault(fieldIndex, "MockValue" + fieldIndex);
    }
    
    public void navigateToMenu(String menuOption) {
        logger.info("Mock navigation to menu: {}", menuOption);
        switch (menuOption.toLowerCase()) {
            case "new business":
                currentScreen = "NEW_BUSINESS";
                break;
            case "client administration":
                currentScreen = "CLIENT_ADMIN";
                break;
            case "new contract":
                currentScreen = "NEW_CONTRACT";
                break;
        }
    }
      // Simulate validation errors
    public boolean validateDate(String date) {
        // More comprehensive date validation for testing
        if (!date.matches("\\d{2}/\\d{2}/\\d{4}")) {
            return false;
        }
        
        // Extract month, day, year
        String[] parts = date.split("/");
        int month = Integer.parseInt(parts[0]);
        int day = Integer.parseInt(parts[1]);
        int year = Integer.parseInt(parts[2]);
        
        // Basic date range validation
        if (month < 1 || month > 12) return false;
        if (day < 1 || day > 31) return false;
        if (year < 1900 || year > 3000) return false;
        
        // More specific validation for months with different day counts
        if (month == 2 && day > 29) return false; // February
        if ((month == 4 || month == 6 || month == 9 || month == 11) && day > 30) return false; // April, June, September, November
        
        return true;
    }
    
    public boolean validateAmount(String amount) {
        try {
            double value = Double.parseDouble(amount);
            return value >= 100; // Minimum amount
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
