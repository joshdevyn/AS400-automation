package ro.nn.qa.automation.terminal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Parser for .ctt (Computer Associates International Terminal Emulator) configuration files
 * Handles production-style .ctt files with comprehensive terminal settings
 */
public class CttConfigParser {
    private static final Logger logger = LoggerFactory.getLogger(CttConfigParser.class);
    
    private Map<String, Properties> sections = new HashMap<>();
    private String filePath;
    
    public CttConfigParser(String filePath) {
        this.filePath = filePath;
    }
    
    /**
     * Parse the .ctt file and extract configuration sections
     */
    public void parse() throws IOException {
        logger.info("Parsing CTT configuration file: {}", filePath);
        
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            String line;
            String currentSection = null;
            Properties currentProps = null;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
                    continue;
                }
                
                // Check for section headers
                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSection = line.substring(1, line.length() - 1);
                    currentProps = new Properties();
                    sections.put(currentSection, currentProps);
                    logger.debug("Found section: {}", currentSection);
                    continue;
                }
                
                // Parse key-value pairs
                if (currentProps != null && line.contains("=")) {
                    int equalIndex = line.indexOf('=');
                    String key = line.substring(0, equalIndex).trim();
                    String value = line.substring(equalIndex + 1).trim();
                    currentProps.setProperty(key, value);
                }
            }
        }
        
        logger.info("Successfully parsed CTT file with {} sections", sections.size());
    }
    
    /**
     * Get connection settings from the CTT file
     */
    public ConnectionSettings getConnectionSettings() {
        ConnectionSettings settings = new ConnectionSettings();
        
        // Extract TCP/IP settings
        Properties tcpSection = sections.get("TCP/IP");
        if (tcpSection != null) {
            String address = tcpSection.getProperty("Address 1", "localhost");
            String port = tcpSection.getProperty("Port 1", "23");
            String timeout = tcpSection.getProperty("Connect Timeout", "30");
            
            settings.setHostname(address);
            settings.setPort(Integer.parseInt(port));
            settings.setConnectTimeout(Integer.parseInt(timeout));
        }
        
        // Extract Communications method
        Properties commSection = sections.get("Communications");
        if (commSection != null) {
            String method = commSection.getProperty("Method", "TCP/IP");
            settings.setMethod(method);
        }
        
        // Extract Terminal settings
        Properties termSection = sections.get("Terminal");
        if (termSection != null) {
            String terminalId = termSection.getProperty("Terminal ID", "F");
            String columns = sections.get("Display") != null ? 
                sections.get("Display").getProperty("Columns", "80") : "80";
            String rows = sections.get("Display") != null ? 
                sections.get("Display").getProperty("Maximum Rows", "25") : "25";
            
            settings.setTerminalId(terminalId);
            settings.setColumns(Integer.parseInt(columns));
            settings.setRows(Integer.parseInt(rows));
        }
        
        return settings;
    }
    
    /**
     * Get a specific section as Properties
     */
    public Properties getSection(String sectionName) {
        return sections.get(sectionName);
    }
    
    /**
     * Get all sections
     */
    public Map<String, Properties> getAllSections() {
        return new HashMap<>(sections);
    }
    
    /**
     * Check if the CTT file contains a specific section
     */
    public boolean hasSection(String sectionName) {
        return sections.containsKey(sectionName);
    }
    
    /**
     * Get a property value from a specific section
     */
    public String getProperty(String section, String key, String defaultValue) {
        Properties props = sections.get(section);
        return props != null ? props.getProperty(key, defaultValue) : defaultValue;
    }
    
    /**
     * Data class for connection settings extracted from CTT file
     */
    public static class ConnectionSettings {
        private String hostname = "localhost";
        private int port = 23;
        private int connectTimeout = 30;
        private String method = "TCP/IP";
        private String terminalId = "F";
        private int columns = 80;
        private int rows = 25;
        
        // Getters and setters
        public String getHostname() { return hostname; }
        public void setHostname(String hostname) { this.hostname = hostname; }
        
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        
        public int getConnectTimeout() { return connectTimeout; }
        public void setConnectTimeout(int connectTimeout) { this.connectTimeout = connectTimeout; }
        
        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
        
        public String getTerminalId() { return terminalId; }
        public void setTerminalId(String terminalId) { this.terminalId = terminalId; }
        
        public int getColumns() { return columns; }
        public void setColumns(int columns) { this.columns = columns; }
        
        public int getRows() { return rows; }
        public void setRows(int rows) { this.rows = rows; }
        
        @Override
        public String toString() {
            return String.format("ConnectionSettings{hostname='%s', port=%d, method='%s', terminalId='%s', size=%dx%d}", 
                hostname, port, method, terminalId, columns, rows);
        }
    }
}
