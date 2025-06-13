package ro.nn.qa.automation.terminal;

import com.jcraft.jsch.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

/**
 * Modern AS400 Terminal implementation using JSch for SSH and Apache Commons Net for Telnet
 * Replaces TN5250j functionality for AS400/IBM i system connections
 */
public class AS400Terminal {
    private static final Logger logger = LoggerFactory.getLogger(AS400Terminal.class);
    
    private String hostname;
    private int port;
    private String username;
    private String password;
    private ConnectionType connectionType;
    private boolean connected = false;
      // SSH connection components
    private com.jcraft.jsch.Session sshSession;
    private Channel sshChannel;
    private InputStream inputStream;
    private OutputStream outputStream;
    
    // Telnet connection components
    private TelnetClient telnetClient;
    
    // Screen buffer for AS400 interactions
    private StringBuilder screenBuffer = new StringBuilder();
    private String currentScreen = "";
    
    public enum ConnectionType {
        JTOPEN, SSH, TELNET
    }
    
    private static ConnectionType resolveConnectionType() {
        Dotenv dotenv = Dotenv.configure()
            .directory("c:/AS400-automation")
            .ignoreIfMissing()
            .load();
        String method = dotenv.get("AS400_TERMINAL_METHOD", "JTOPEN").toUpperCase();
        try {
            return ConnectionType.valueOf(method);
        } catch (Exception e) {
            return ConnectionType.JTOPEN;
        }
    }

    /**
     * Constructor for AS400 Terminal
     * @param hostname AS400 system hostname or IP
     * @param port Connection port (typically 22 for SSH, 23 for Telnet)
     * @param username AS400 username
     * @param password AS400 password
     * @param connectionType SSH or TELNET
     */
    public AS400Terminal(String hostname, int port, String username, String password, ConnectionType connectionType) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
        // Use env-based connection type if not explicitly provided
        if (connectionType == null) {
            this.connectionType = resolveConnectionType();
        } else {
            this.connectionType = connectionType;
        }
        // Only allow supported methods
        if (this.connectionType != ConnectionType.JTOPEN && this.connectionType != ConnectionType.SSH && this.connectionType != ConnectionType.TELNET) {
            throw new UnsupportedOperationException("Unsupported AS400 terminal method: " + this.connectionType);
        }
    }

    public AS400Terminal(String hostname, int port, String username, String password) {
        this(hostname, port, username, password, resolveConnectionType());
    }
    
    /**
     * Constructor that accepts a CTT configuration file
     * @param cttFilePath Path to the .ctt configuration file
     * @param username AS400 username
     * @param password AS400 password
     */
    public AS400Terminal(String cttFilePath, String username, String password) throws IOException {
        this.username = username;
        this.password = password;
        
        // Parse CTT file and extract connection settings
        CttConfigParser parser = new CttConfigParser(cttFilePath);
        parser.parse();
        
        CttConfigParser.ConnectionSettings settings = parser.getConnectionSettings();
        this.hostname = settings.getHostname();
        this.port = settings.getPort();
        this.connectionType = resolveConnectionType();
        
        logger.info("Initialized AS400Terminal from CTT file: {}", settings);
    }

    /**
     * Connect to AS400 system
     */
    public void connect() throws Exception {
        logger.info("Connecting to AS400 system: {}:{} via {}", hostname, port, connectionType);
        switch (connectionType) {
            case JTOPEN:
                // TODO: Implement JTOpen connection logic here
                throw new UnsupportedOperationException("JTOpen connection not yet implemented in this class");
            case SSH:
                connectSSH();
                connected = true;
                break;
            case TELNET:
                connectTelnet();
                break;
            default:
                throw new IllegalArgumentException("Unsupported connection type: " + connectionType);
        }
        logger.info("Successfully connected to AS400 system");
    }
    
    /**
     * SSH connection using JSch
     */
    private void connectSSH() throws Exception {
        JSch jsch = new JSch();
        
        // Configure SSH session
        sshSession = jsch.getSession(username, hostname, port);
        sshSession.setPassword(password);
        
        // Configure SSH properties for AS400
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        config.put("PreferredAuthentications", "password");
        config.put("ServerAliveInterval", "60");
        config.put("ServerAliveCountMax", "3");
        sshSession.setConfig(config);
        
        // Connect SSH session
        sshSession.connect(30000); // 30 second timeout
        
        // Open shell channel for interactive session
        sshChannel = sshSession.openChannel("shell");
        
        // Configure terminal type for AS400
        ((ChannelShell) sshChannel).setPtyType("vt320");
        ((ChannelShell) sshChannel).setPtySize(80, 24, 640, 480);
        
        // Get input/output streams
        inputStream = sshChannel.getInputStream();
        outputStream = sshChannel.getOutputStream();
        
        sshChannel.connect();
          // Wait for initial prompt
        Thread.sleep(2000);
        readCurrentScreen();
    }
    
    /**
     * Telnet connection using Apache Commons Net
     */
    private void connectTelnet() throws Exception {
        telnetClient = new TelnetClient();
        telnetClient.setConnectTimeout(30000);
        
        // Connect to AS400
        telnetClient.connect(hostname, port);
        
        inputStream = telnetClient.getInputStream();
        outputStream = telnetClient.getOutputStream();
        
        // Set connected flag early so sendText() works
        connected = true;
        
        // Read initial screen
        Thread.sleep(1000);
        readCurrentScreen();
        
        logger.debug("Initial screen received: {}", currentScreen.substring(0, Math.min(100, currentScreen.length())));
        
        // AS400 login sequence - look for sign on screen
        if (!waitForText("Sign On", 5000)) {
            throw new Exception("AS400 Sign On screen not found");
        }
        
        // Send login credentials
        sendText(username);
        sendText(password);
        
        // Wait for main menu
        waitForText("MAIN", 15000);
    }
    
    /**
     * Send text to AS400 terminal
     */
    public void sendText(String text) throws Exception {
        if (!connected) {
            throw new IllegalStateException("Not connected to AS400");
        }
        
        logger.debug("Sending text: {}", text);
        outputStream.write((text + "\r\n").getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        
        // Small delay to allow AS400 to process
        Thread.sleep(500);
        readCurrentScreen();
    }
    
    /**
     * Send function key (F1-F24)
     */
    public void sendFunctionKey(int functionKey) throws Exception {
        if (functionKey < 1 || functionKey > 24) {
            throw new IllegalArgumentException("Function key must be between 1 and 24");
        }
        
        String functionKeyCode = getFunctionKeyCode(functionKey);
        logger.debug("Sending function key F{}: {}", functionKey, functionKeyCode);
        
        outputStream.write(functionKeyCode.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        
        Thread.sleep(1000); // Allow time for screen refresh
        readCurrentScreen();
    }
    
    /**
     * Get AS400 function key escape sequence
     */
    private String getFunctionKeyCode(int functionKey) {
        // VT320 function key sequences for AS400
        switch (functionKey) {
            case 1: return "\u001b[11~";  // F1
            case 2: return "\u001b[12~";  // F2
            case 3: return "\u001b[13~";  // F3
            case 4: return "\u001b[14~";  // F4
            case 5: return "\u001b[15~";  // F5
            case 6: return "\u001b[17~";  // F6
            case 7: return "\u001b[18~";  // F7
            case 8: return "\u001b[19~";  // F8
            case 9: return "\u001b[20~";  // F9
            case 10: return "\u001b[21~"; // F10
            case 11: return "\u001b[23~"; // F11
            case 12: return "\u001b[24~"; // F12
            default: return "\u001b[" + (functionKey + 10) + "~";
        }
    }
    
    /**
     * Wait for specific text to appear on screen
     */
    public boolean waitForText(String expectedText, long timeoutMs) throws Exception {
        long startTime = System.currentTimeMillis();
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            readCurrentScreen();
            if (currentScreen.contains(expectedText)) {
                logger.debug("Found expected text: {}", expectedText);
                return true;
            }
            Thread.sleep(100);
        }
          logger.warn("Timeout waiting for text: {}", expectedText);
        return false;
    }
    
    /**
     * Read current screen content
     */
    public void readCurrentScreen() throws Exception {
        // Read all available data with a small timeout
        byte[] buffer = new byte[8192];
        int totalBytesRead = 0;
        
        // Wait a bit for data to arrive
        Thread.sleep(100);
        
        // Read all available data
        while (inputStream.available() > 0) {
            int bytesRead = inputStream.read(buffer, totalBytesRead, buffer.length - totalBytesRead);
            if (bytesRead > 0) {
                totalBytesRead += bytesRead;
                if (totalBytesRead >= buffer.length) {
                    break; // Buffer full
                }
            } else {
                break; // No more data
            }
            Thread.sleep(50); // Small delay for more data
        }
        
        if (totalBytesRead > 0) {
            String newData = new String(buffer, 0, totalBytesRead, StandardCharsets.UTF_8);
            screenBuffer.setLength(0); // Clear old data
            screenBuffer.append(newData);
            currentScreen = screenBuffer.toString();
            logger.trace("Screen updated: {} bytes, content: {}", totalBytesRead, 
                currentScreen.length() > 200 ? currentScreen.substring(0, 200) + "..." : currentScreen);
        }
    }
    
    /**
     * Get current screen text
     */
    public String getCurrentScreen() {
        return currentScreen;
    }
    
    /**
     * Get current screen as AS400Screen object for field parsing
     */
    public AS400Screen getScreen() {
        return new AS400Screen(this);
    }
    
    /**
     * Send special keys (ENTER, TAB, etc.)
     */
    public void sendKey(String keyName) throws Exception {
        if (!connected) {
            throw new IllegalStateException("Not connected to AS400");
        }
        
        String keyCode;
        switch (keyName.toUpperCase()) {
            case "ENTER":
                keyCode = "\r\n";
                break;
            case "TAB":
                keyCode = "\t";
                break;
            case "ESCAPE":
                keyCode = "\u001b";
                break;
            default:
                throw new IllegalArgumentException("Unsupported key: " + keyName);
        }
        
        logger.debug("Sending key: {}", keyName);
        outputStream.write(keyCode.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        
        Thread.sleep(500);
        readCurrentScreen();
    }
    
    /**
     * Wait for response with timeout
     */
    public void waitForResponse(long timeoutMs) throws Exception {
        Thread.sleep(timeoutMs);
        readCurrentScreen();
    }
    
    /**
     * Login method for backward compatibility with tests
     * Note: In modern implementation, authentication is handled in connect()
     * This method validates if the current screen shows successful login
     */
    public boolean login(String username, String password, String library) {
        try {
            // Check if we're already connected and authenticated
            if (connected && isTextOnScreen("MAIN")) {
                return true;
            }
            
            // If not connected, attempt connection with provided credentials
            if (!connected) {
                this.username = username;
                this.password = password;
                connect();
                return connected;
            }
            
            // If connected but not authenticated, look for login prompt
            if (isTextOnScreen("USER") || isTextOnScreen("PASSWORD")) {
                // Send username
                sendText(username);
                sendKey("ENTER");
                waitForResponse(2000);
                
                // Send password
                sendText(password);
                sendKey("ENTER");
                waitForResponse(2000);
                
                // Send library if needed
                if (library != null && isTextOnScreen("LIBRARY")) {
                    sendText(library);
                    sendKey("ENTER");
                    waitForResponse(2000);
                }
                
                // Check if login was successful
                return isTextOnScreen("MAIN") || isTextOnScreen("MENU");
            }
            
            return connected;
        } catch (Exception e) {
            logger.error("Login failed", e);
            return false;
        }
    }
    
    /**
     * Get frame object for backward compatibility
     * Returns a simple frame wrapper that provides session closure capability
     */
    public TerminalFrame getFrame() {
        return new TerminalFrame(this);
    }
    
    // Inner class to provide frame compatibility
    public static class TerminalFrame {
        private AS400Terminal terminal;
        
        public TerminalFrame(AS400Terminal terminal) {
            this.terminal = terminal;
        }
        
        public void closeSession() {
            if (terminal != null) {
                terminal.disconnect();
            }
        }
    }

    /**
     * Check if text exists on current screen
     */
    public boolean isTextOnScreen(String text) {
        return currentScreen.contains(text);
    }
    
    /**
     * Clear screen buffer
     */
    public void clearScreen() {
        screenBuffer.setLength(0);
        currentScreen = "";
        logger.debug("Screen buffer cleared");
    }
    
    /**
     * Check if connected to AS400
     */
    public boolean isConnected() {
        return connected && 
               ((sshSession != null && sshSession.isConnected()) || 
                (telnetClient != null && telnetClient.isConnected()));
    }
    
    /**
     * Disconnect from AS400
     */
    public void disconnect() {
        logger.info("Disconnecting from AS400 system");
        
        try {
            if (sshChannel != null && sshChannel.isConnected()) {
                sshChannel.disconnect();
            }
            if (sshSession != null && sshSession.isConnected()) {
                sshSession.disconnect();
            }
            if (telnetClient != null && telnetClient.isConnected()) {
                telnetClient.disconnect();
            }
        } catch (Exception e) {
            logger.error("Error during disconnect", e);
        } finally {
            connected = false;
            sshSession = null;
            sshChannel = null;
            telnetClient = null;
            inputStream = null;
            outputStream = null;
        }
        
        logger.info("Disconnected from AS400 system");
    }
    
    /**
     * Execute AS400 command and return result
     */
    public CompletableFuture<String> executeCommand(String command) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                sendText(command);
                Thread.sleep(2000); // Wait for command execution
                readCurrentScreen();
                return currentScreen;
            } catch (Exception e) {
                logger.error("Error executing command: " + command, e);
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Navigate to AS400 menu option
     */
    public void navigateToMenu(String menuOption) throws Exception {
        logger.info("Navigating to menu option: {}", menuOption);
        sendText(menuOption);
        Thread.sleep(1500); // Allow navigation time
        readCurrentScreen();
    }
    
    /**
     * Enter data into AS400 field
     */
    public void enterField(String fieldValue) throws Exception {
        logger.debug("Entering field value: {}", fieldValue);
        sendText(fieldValue);
        sendText("\t"); // Tab to next field
    }
    
    /**
     * Press Enter key
     */
    public void pressEnter() throws Exception {
        logger.debug("Pressing Enter");
        outputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        Thread.sleep(1000);
        readCurrentScreen();
    }
    
    /**
     * Navigates back to the main menu of the AS400 system.
     * This method sends the appropriate command or key sequence to return to the main menu.
     */
    public void returnToMainMenu() throws IOException {
        if (connectionType == ConnectionType.TELNET && telnetClient != null && telnetClient.isConnected()) {
            // Example: Send F3 key sequence to return to the main menu
            outputStream.write("\u001B[3~".getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            logger.info("Navigated back to the main menu.");
        } else if (connectionType == ConnectionType.SSH && sshChannel != null && sshChannel.isConnected()) {
            // Example: Send F3 key sequence for SSH
            outputStream.write("\u001B[3~".getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            logger.info("Navigated back to the main menu.");
        } else {
            logger.warn("Cannot navigate to the main menu. No active connection.");
        }
    }
}
