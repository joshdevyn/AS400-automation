package ro.nn.qa.automation.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AS400 Terminal Simulator Server
 * Simulates AS400 terminal sessions over Telnet protocol
 * Provides realistic AS400 screens and navigation for testing
 */
public class AS400SimulatorServer {
    private static final Logger logger = LoggerFactory.getLogger(AS400SimulatorServer.class);
    
    private final int port;
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private boolean running = false;
    private Map<String, AS400Session> activeSessions = new ConcurrentHashMap<>();
    
    // AS400 Screen Templates
    private static final Map<String, String> SCREEN_TEMPLATES = new HashMap<>();
    
    static {
        initializeScreenTemplates();
    }
    
    public AS400SimulatorServer(int port) {
        this.port = port;
        this.executorService = Executors.newCachedThreadPool();
    }
    
    private static void initializeScreenTemplates() {
        // Sign-on screen
        SCREEN_TEMPLATES.put("SIGNON", 
            "                             Sign On                               \n" +
            "                                                                   \n" +
            " System . . . . :   AS400SIM                                      \n" +
            " Subsystem  . . :   QINTER                                        \n" +
            " Display  . . . :   QPADEV0001                                    \n" +
            "                                                                   \n" +
            " User . . . . . . :   ________                                    \n" +
            " Password . . . . :   ________                                    \n" +
            " Program/procedure:   ________                                    \n" +
            " Menu . . . . . . :   ________                                    \n" +
            " Current library  :   ________                                    \n" +
            "                                                                   \n" +
            "                                                                   \n" +
            " (C) COPYRIGHT IBM CORP. 1980, 2023.                             \n" +
            "                                                                   \n" +
            "                                                                   \n" +
            "                                                                   \n" +
            "                                                                   \n" +
            "                                                                   \n" +
            "                                                                   \n" +
            "                                                                   \n" +
            "                                                                   \n" +
            "                                                                   \n" +
            "                                                                   \n"
        );
        
        // Main menu
        SCREEN_TEMPLATES.put("MAIN", 
            "                              MAIN MENU                           \n" +
            "                                                                   \n" +
            " System: AS400SIM                                                 \n" +
            "                                                                   \n" +
            " Select one of the following:                                     \n" +
            "                                                                   \n" +
            "      1. User tasks                                               \n" +
            "      2. Office tasks                                             \n" +
            "      3. General system tasks                                     \n" +
            "      4. Files, libraries, and folders                           \n" +
            "      5. Programming                                              \n" +
            "      6. Communications                                           \n" +
            "      7. Define or change the system                             \n" +
            "      8. Problem handling                                         \n" +
            "      9. Display a menu                                           \n" +
            "     10. Information Assistant options                            \n" +
            "     11. Client Access/400 tasks                                  \n" +
            "                                                                   \n" +
            " Selection or command                                             \n" +
            " ===> _                                                           \n" +
            "                                                                   \n" +
            " F3=Exit   F4=Prompt   F9=Retrieve   F12=Cancel                  \n" +
            " F13=Information Assistant   F23=Set initial menu                \n" +
            "                                                                   \n" +
            "                                                                   \n" +
            "                                                                   \n"
        );
        
        // Business application menu
        SCREEN_TEMPLATES.put("BUSINESS", 
            "                          BUSINESS APPLICATIONS                   \n" +
            "                                                                   \n" +
            " System: AS400SIM                                                 \n" +
            "                                                                   \n" +
            " Select one of the following:                                     \n" +
            "                                                                   \n" +
            "      1. New Contract Entry                                       \n" +
            "      2. Contract Maintenance                                     \n" +
            "      3. Client Administration                                    \n" +
            "      4. Reports                                                  \n" +
            "      5. Endowment Processing                                     \n" +
            "      6. Policy Management                                        \n" +
            "      7. Claims Processing                                        \n" +
            "      8. Financial Operations                                     \n" +
            "      9. System Utilities                                         \n" +
            "                                                                   \n" +
            "                                                                   \n" +
            " Selection or command                                             \n" +
            " ===> _                                                           \n" +
            "                                                                   \n" +
            " F3=Exit   F4=Prompt   F9=Retrieve   F12=Cancel                  \n" +
            "                                                                   \n" +
            "                                                                   \n" +
            "                                                                   \n"
        );
        
        // New Contract screen
        SCREEN_TEMPLATES.put("NEWCONTRACT", 
            "                          NEW CONTRACT ENTRY                      \n" +
            "                                                                   \n" +
            " Contract Type: ___                                               \n" +
            " Policy Number: ___________                                       \n" +
            "                                                                   \n" +
            " Client Information:                                              \n" +
            " Last Name  : ____________________                                \n" +
            " First Name : ____________________                                \n" +
            " CNP        : _______________                                     \n" +
            " Phone      : ____________________                                \n" +
            " Address    : ____________________                                \n" +
            "              ____________________                                \n" +
            " City       : ____________________                                \n" +
            " County     : ____________________                                \n" +
            "                                                                   \n" +
            " Premium Amount: ____________                                     \n" +
            " Currency      : ___                                              \n" +
            "                                                                   \n" +
            " F3=Exit   F5=Refresh   F12=Cancel   F24=More Keys               \n" +
            "                                                                   \n"
        );
    }
    
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        
        logger.info("AS400 Simulator Server started on port {}", port);
        logger.info("Available screens: {}", SCREEN_TEMPLATES.keySet());
        
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                String sessionId = generateSessionId();
                AS400Session session = new AS400Session(sessionId, clientSocket);
                activeSessions.put(sessionId, session);
                
                logger.info("New client connected: {} (Session: {})", 
                    clientSocket.getRemoteSocketAddress(), sessionId);
                
                executorService.submit(session);
            } catch (IOException e) {
                if (running) {
                    logger.error("Error accepting client connection", e);
                }
            }
        }
    }
    
    public void stop() {
        running = false;
        
        // Close all active sessions
        activeSessions.values().forEach(AS400Session::close);
        activeSessions.clear();
        
        // Close server socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.error("Error closing server socket", e);
            }
        }
        
        // Shutdown executor
        executorService.shutdown();
        
        logger.info("AS400 Simulator Server stopped");
    }
    
    private String generateSessionId() {
        return "SIM" + System.currentTimeMillis() % 10000;
    }
    
    /**
     * Individual AS400 session handler
     */
    private static class AS400Session implements Runnable {
        private final String sessionId;
        private final Socket socket;
        private final BufferedReader reader;
        private final PrintWriter writer;
        private String currentScreen = "SIGNON";
        private Map<String, String> sessionData = new HashMap<>();
        private boolean authenticated = false;
        
        public AS400Session(String sessionId, Socket socket) throws IOException {
            this.sessionId = sessionId;
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        }
        
        @Override
        public void run() {
            try {
                // Send initial screen
                sendScreen(currentScreen);
                
                String input;
                while ((input = reader.readLine()) != null && !socket.isClosed()) {
                    logger.debug("Session {}: Received input: {}", sessionId, input);
                    
                    processInput(input.trim());
                    
                    // Send current screen
                    sendScreen(currentScreen);
                }
            } catch (IOException e) {
                logger.error("Session {} error", sessionId, e);
            } finally {
                close();
            }
        }
        
        private void processInput(String input) {
            switch (currentScreen) {
                case "SIGNON":
                    processSignonInput(input);
                    break;
                case "MAIN":
                    processMainMenuInput(input);
                    break;
                case "BUSINESS":
                    processBusinessMenuInput(input);
                    break;
                case "NEWCONTRACT":
                    processNewContractInput(input);
                    break;
                default:
                    logger.warn("Unknown screen: {}", currentScreen);
                    break;
            }
        }
        
        private void processSignonInput(String input) {
            // Parse signon fields
            if (input.contains("GIUROAL") || input.toUpperCase().contains("USER")) {
                sessionData.put("user", "GIUROAL");
            }
            if (input.contains("Bucuresti2") || input.toUpperCase().contains("PASSWORD")) {
                sessionData.put("password", "Bucuresti2");
            }
            
            // Check for ENTER key or complete authentication
            if (input.isEmpty() || input.equals("ENTER") || 
                (sessionData.containsKey("user") && sessionData.containsKey("password"))) {
                
                if ("GIUROAL".equals(sessionData.get("user")) && 
                    "Bucuresti2".equals(sessionData.get("password"))) {
                    authenticated = true;
                    currentScreen = "MAIN";
                    logger.info("Session {} authenticated successfully", sessionId);
                } else {
                    // Send error message and stay on signon
                    writer.println("Invalid username or password. Please try again.");
                }
            }
        }
        
        private void processMainMenuInput(String input) {
            if (input.equals("1") || input.toUpperCase().contains("BUSINESS")) {
                currentScreen = "BUSINESS";
            } else if (input.equals("F3") || input.toUpperCase().contains("EXIT")) {
                currentScreen = "SIGNON";
                authenticated = false;
                sessionData.clear();
            }
        }
        
        private void processBusinessMenuInput(String input) {
            if (input.equals("1") || input.toUpperCase().contains("CONTRACT")) {
                currentScreen = "NEWCONTRACT";
            } else if (input.equals("5") || input.toUpperCase().contains("ENDOWMENT")) {
                currentScreen = "ENDOWMENT";
            } else if (input.equals("F3") || input.toUpperCase().contains("EXIT")) {
                currentScreen = "MAIN";
            }
        }
        
        private void processNewContractInput(String input) {
            // Process contract data entry
            if (input.equals("F3") || input.toUpperCase().contains("EXIT")) {
                currentScreen = "BUSINESS";
            } else if (input.equals("F12") || input.toUpperCase().contains("CANCEL")) {
                currentScreen = "BUSINESS";
            }
            // Store field data
            sessionData.put("contract_data", input);
        }
        
        private void sendScreen(String screenName) {
            String screen = SCREEN_TEMPLATES.get(screenName);
            if (screen != null) {
                writer.println(screen);
                writer.flush();
                logger.debug("Session {}: Sent screen {}", sessionId, screenName);
            } else {
                writer.println("Screen not found: " + screenName);
                logger.error("Session {}: Screen not found: {}", sessionId, screenName);
            }
        }
        
        public void close() {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                }
                logger.info("Session {} closed", sessionId);
            } catch (IOException e) {
                logger.error("Error closing session {}", sessionId, e);
            }
        }
    }
    
    /**
     * Main method to start the server
     */
    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 23;
        
        AS400SimulatorServer server = new AS400SimulatorServer(port);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        
        try {
            server.start();
        } catch (IOException e) {
            logger.error("Failed to start AS400 simulator server", e);
        }
    }
}
