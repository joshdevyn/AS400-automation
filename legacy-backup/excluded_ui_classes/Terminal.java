package ro.nn.qa.automation.terminal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Modern Terminal class - Wrapper around AS400Terminal
 * Replaces TN5250j-based Terminal implementation
 * Created by Alexandru Giurovici on 01.09.2015.
 * Modernized to use JSch-based AS400Terminal instead of TN5250j
 */
public class Terminal {
    private static final Logger logger = LoggerFactory.getLogger(Terminal.class);
    
    private AS400Terminal as400Terminal;
    private AS400Screen currentScreen;
    private String hostname;
    private int port;
    private String username;
    private String password;
    private AS400Terminal.ConnectionType connectionType;
    private boolean connected = false;
    
    /**
     * Default constructor
     */
    public Terminal() {
        // Default constructor
    }
    
    /**
     * Constructor with connection parameters
     */
    public Terminal(String hostname, int port, String username, String password) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
        this.connectionType = AS400Terminal.ConnectionType.SSH; // Default to SSH
    }
    
    /**
     * Constructor with connection type
     */
    public Terminal(String hostname, int port, String username, String password, AS400Terminal.ConnectionType connectionType) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
        this.connectionType = connectionType;
    }
    
    /**
     * Initialize terminal with properties
     */
    public void initialize(Properties sessionProperties) {
        logger.info("Initializing terminal with properties");
        
        if (sessionProperties != null) {
            this.hostname = sessionProperties.getProperty("hostname", this.hostname);
            this.port = Integer.parseInt(sessionProperties.getProperty("port", String.valueOf(this.port)));
            this.username = sessionProperties.getProperty("username", this.username);
            this.password = sessionProperties.getProperty("password", this.password);
            
            String connType = sessionProperties.getProperty("connectionType", "SSH");
            this.connectionType = AS400Terminal.ConnectionType.valueOf(connType.toUpperCase());
        }
        
        // Create AS400Terminal instance
        this.as400Terminal = new AS400Terminal(hostname, port, username, password, connectionType);
        
        logger.info("Terminal initialized for {}:{} via {}", hostname, port, connectionType);
    }
    
    /**
     * Start terminal session
     */
    public void start() throws Exception {
        logger.info("Starting terminal session");
        
        if (as400Terminal == null) {
            throw new IllegalStateException("Terminal not initialized. Call initialize() first.");
        }
        
        as400Terminal.connect();
        this.currentScreen = new AS400Screen(as400Terminal);
        this.connected = true;
        
        logger.info("Terminal session started successfully");
    }
    
    /**
     * Start terminal with session arguments (legacy compatibility)
     */
    public void start(String[] sessionArgs) throws Exception {
        // Parse legacy session arguments if needed
        if (sessionArgs != null && sessionArgs.length > 0) {
            logger.debug("Processing session arguments: {}", String.join(" ", sessionArgs));
            // Add argument parsing logic here if needed
        }
        
        start();
    }
    
    /**
     * Stop terminal session
     */
    public void stop() {
        logger.info("Stopping terminal session");
        
        if (as400Terminal != null) {
            as400Terminal.disconnect();
        }
        
        this.connected = false;
        this.currentScreen = null;
        
        logger.info("Terminal session stopped");
    }
    
    /**
     * Get current screen
     */
    public AS400Screen getScreen() {
        if (currentScreen == null && as400Terminal != null) {
            currentScreen = new AS400Screen(as400Terminal);
        }
        return currentScreen;
    }
    
    /**
     * Get AS400 terminal instance
     */
    public AS400Terminal getAS400Terminal() {
        return as400Terminal;
    }
    
    /**
     * Check if terminal is connected
     */
    public boolean isConnected() {
        return connected && as400Terminal != null && as400Terminal.isConnected();
    }
    
    /**
     * Send text to terminal
     */
    public void sendText(String text) throws Exception {
        if (as400Terminal != null) {
            as400Terminal.sendText(text);
            refreshScreen();
        }
    }
    
    /**
     * Send function key
     */
    public void sendFunctionKey(int functionKey) throws Exception {
        if (as400Terminal != null) {
            as400Terminal.sendFunctionKey(functionKey);
            refreshScreen();
        }
    }
    
    /**
     * Press Enter
     */
    public void pressEnter() throws Exception {
        if (as400Terminal != null) {
            as400Terminal.pressEnter();
            refreshScreen();
        }
    }
    
    /**
     * Navigate to menu
     */
    public void navigateToMenu(String menuOption) throws Exception {
        if (as400Terminal != null) {
            as400Terminal.navigateToMenu(menuOption);
            refreshScreen();
        }
    }
    
    /**
     * Wait for text to appear
     */
    public boolean waitForText(String text, long timeoutMs) throws Exception {
        if (as400Terminal != null) {
            boolean found = as400Terminal.waitForText(text, timeoutMs);
            if (found) {
                refreshScreen();
            }
            return found;
        }
        return false;
    }
    
    /**
     * Refresh current screen
     */
    public void refreshScreen() {
        if (currentScreen != null) {
            currentScreen.refresh();
        }
    }
    
    /**
     * Get screen text
     */
    public String getScreenText() {
        return currentScreen != null ? currentScreen.getText() : "";
    }
    
    /**
     * Check if text is on screen
     */
    public boolean isTextOnScreen(String text) {
        return currentScreen != null && currentScreen.containsText(text);
    }
    
    /**
     * Legacy method - get session panel (returns screen)
     */
    public AS400Screen getSessionPanel() {
        return getScreen();
    }
    
    /**
     * Legacy method - set visible
     */
    public void setVisible(boolean visible) {
        logger.debug("setVisible called with: {} (no-op in modern implementation)", visible);
        // No-op for modern implementation - screens are always "visible" in terminal
    }
    
    /**
     * Legacy method - add session listener
     */
    public void addSessionListener(Object listener) {
        logger.debug("addSessionListener called (no-op in modern implementation)");
        // No-op for modern implementation - use direct method calls instead
    }
    
    /**
     * Legacy method - remove session listener
     */
    public void removeSessionListener(Object listener) {
        logger.debug("removeSessionListener called (no-op in modern implementation)");
        // No-op for modern implementation
    }
    
    /**
     * Legacy method - get configuration
     */
    public Properties getConfiguration() {
        Properties config = new Properties();
        config.setProperty("hostname", hostname != null ? hostname : "");
        config.setProperty("port", String.valueOf(port));
        config.setProperty("username", username != null ? username : "");
        config.setProperty("connectionType", connectionType != null ? connectionType.toString() : "SSH");
        return config;
    }
    
    /**
     * Set connection parameters
     */
    public void setConnectionParameters(String hostname, int port, String username, String password) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
        
        logger.info("Connection parameters updated for {}:{}", hostname, port);
    }
    
    /**
     * Set connection type
     */
    public void setConnectionType(AS400Terminal.ConnectionType connectionType) {
        this.connectionType = connectionType;
        logger.debug("Connection type set to: {}", connectionType);
    }
    
    /**
     * Execute command on AS400
     */
    public String executeCommand(String command) throws Exception {
        if (as400Terminal != null) {
            return as400Terminal.executeCommand(command).get();
        }
        return "";
    }
    
    /**
     * Check if on main menu
     */
    public boolean isOnMainMenu() {
        return currentScreen != null && currentScreen.isMainMenu();
    }
    
    /**
     * Return to main menu
     */
    public void returnToMainMenu() throws Exception {
        logger.info("Returning to main menu");
        
        int attempts = 0;
        int maxAttempts = 5;
        
        while (!isOnMainMenu() && attempts < maxAttempts) {
            sendFunctionKey(3); // F3 - Exit
            Thread.sleep(1000);
            attempts++;
        }
        
        if (!isOnMainMenu()) {
            logger.warn("Could not return to main menu after {} attempts", maxAttempts);
        } else {
            logger.info("Successfully returned to main menu");
        }
    }
    
    @Override
    public String toString() {
        return "Terminal{" +
                "hostname='" + hostname + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", connectionType=" + connectionType +
                ", connected=" + connected +
                '}';
    }
}
