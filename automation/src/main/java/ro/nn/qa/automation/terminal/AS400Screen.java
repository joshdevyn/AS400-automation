package ro.nn.qa.automation.terminal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Modern AS400 Screen representation
 * Replaces TN5250j Screen5250 functionality
 */
public class AS400Screen {
    private static final Logger logger = LoggerFactory.getLogger(AS400Screen.class);
    
    private AS400Terminal terminal;
    private String screenText;
    private List<AS400Field> fields;
    private static final int SCREEN_WIDTH = 80;
    private static final int SCREEN_HEIGHT = 24;
    
    public AS400Screen(AS400Terminal terminal) {
        this.terminal = terminal;
        this.fields = new ArrayList<>();
        refresh();
    }
    
    /**
     * Refresh screen content from terminal
     */
    public void refresh() {
        try {
            terminal.readCurrentScreen();
            this.screenText = terminal.getCurrentScreen();
            parseFields();
            logger.debug("Screen refreshed, {} fields found", fields.size());
        } catch (Exception e) {
            logger.error("Error refreshing screen", e);
        }
    }
    
    /**
     * Parse screen fields from terminal output
     */
    private void parseFields() {
        fields.clear();
        if (screenText == null || screenText.trim().isEmpty()) {
            return;
        }
        
        // Look for common AS400 field patterns
        // Input fields typically appear as underscores or dots
        Pattern fieldPattern = Pattern.compile("([_.]{2,})");
        Matcher matcher = fieldPattern.matcher(screenText);
        
        int fieldNumber = 0;
        while (matcher.find()) {
            String fieldMarker = matcher.group(1);
            int startPos = matcher.start();
            int endPos = matcher.end();
            
            AS400Field field = new AS400Field(
                fieldNumber++,
                startPos,
                endPos,
                fieldMarker.length(),
                ""
            );
            fields.add(field);
        }
        
        // Also look for labeled fields (common AS400 pattern)
        Pattern labelPattern = Pattern.compile("([A-Za-z][A-Za-z0-9\\s]*?)\\s*[.:]\\s*([_.]{2,})");
        Matcher labelMatcher = labelPattern.matcher(screenText);
        
        while (labelMatcher.find()) {
            String label = labelMatcher.group(1).trim();
            String fieldMarker = labelMatcher.group(2);
            int startPos = labelMatcher.start(2);
            int endPos = labelMatcher.end(2);
            
            AS400Field field = new AS400Field(
                fieldNumber++,
                startPos,
                endPos,
                fieldMarker.length(),
                ""
            );
            field.setLabel(label);
            fields.add(field);
        }
    }
    
    /**
     * Get screen text content
     */
    public String getText() {
        return screenText != null ? screenText : "";
    }
    
    /**
     * Check if text exists on screen
     */
    public boolean containsText(String text) {
        return screenText != null && screenText.contains(text);
    }
    
    /**
     * Get all fields on screen
     */
    public List<AS400Field> getFields() {
        return new ArrayList<>(fields);
    }
    
    /**
     * Get field by label
     */
    public AS400Field getFieldByLabel(String label) {
        return fields.stream()
                .filter(field -> label.equalsIgnoreCase(field.getLabel()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Get field by position
     */
    public AS400Field getFieldByPosition(int position) {
        return fields.stream()
                .filter(field -> position >= field.getStartPosition() && position <= field.getEndPosition())
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Get field by index
     */
    public AS400Field getField(int index) {
        if (index >= 0 && index < fields.size()) {
            return fields.get(index);
        }
        return null;
    }
    
    /**
     * Set field value by label
     */
    public void setFieldValue(String label, String value) throws Exception {
        AS400Field field = getFieldByLabel(label);
        if (field != null) {
            field.setValue(value);
            // Position cursor and enter value
            positionCursor(field.getStartPosition());
            terminal.sendText(value);
            logger.debug("Set field '{}' to value '{}'", label, value);
        } else {
            logger.warn("Field not found: {}", label);
        }
    }
    
    /**
     * Set field value by index
     */
    public void setFieldValue(int fieldIndex, String value) throws Exception {
        AS400Field field = getField(fieldIndex);
        if (field != null) {
            field.setValue(value);
            positionCursor(field.getStartPosition());
            terminal.sendText(value);
            logger.debug("Set field {} to value '{}'", fieldIndex, value);
        } else {
            logger.warn("Field not found at index: {}", fieldIndex);
        }
    }
    
    /**
     * Position cursor at specific screen position
     */
    private void positionCursor(int position) throws Exception {
        // Calculate row and column from linear position
        int row = position / SCREEN_WIDTH;
        int col = position % SCREEN_WIDTH;
        
        // Send cursor positioning escape sequence
        String cursorPosition = String.format("\u001b[%d;%dH", row + 1, col + 1);
        terminal.sendText(cursorPosition);
    }
    
    /**
     * Press function key
     */
    public void pressFunctionKey(int functionKey) throws Exception {
        terminal.sendFunctionKey(functionKey);
        refresh(); // Refresh screen after function key
    }
    
    /**
     * Press Enter key
     */
    public void pressEnter() throws Exception {
        terminal.pressEnter();
        refresh(); // Refresh screen after Enter
    }
    
    /**
     * Wait for specific text to appear on screen
     */
    public boolean waitForText(String text, long timeoutMs) throws Exception {
        boolean found = terminal.waitForText(text, timeoutMs);
        if (found) {
            refresh();
        }
        return found;
    }
    
    /**
     * Wait for screen to change
     */
    public boolean waitForScreenChange(long timeoutMs) throws Exception {
        String originalScreen = screenText;
        long startTime = System.currentTimeMillis();
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            refresh();
            if (!getText().equals(originalScreen)) {
                logger.debug("Screen changed detected");
                return true;
            }
            Thread.sleep(100);
        }
        
        logger.warn("Timeout waiting for screen change");
        return false;
    }
    
    /**
     * Get screen lines as array
     */
    public String[] getLines() {
        if (screenText == null) {
            return new String[0];
        }
        return screenText.split("\\r?\\n");
    }
    
    /**
     * Get specific line from screen
     */
    public String getLine(int lineNumber) {
        String[] lines = getLines();
        if (lineNumber >= 0 && lineNumber < lines.length) {
            return lines[lineNumber];
        }
        return "";
    }
    
    /**
     * Check if current screen is the main menu
     */
    public boolean isMainMenu() {
        return containsText("MAIN MENU") || containsText("Main Menu");
    }
    
    /**
     * Check if current screen is a selection list (F4 help)
     */
    public boolean isSelectionList() {
        return containsText("Select one of the following") || 
               containsText("Position to") ||
               containsText("Type choices");
    }
    
    /**
     * Check if current screen has errors
     */
    public boolean hasErrors() {
        return containsText("Error") || 
               containsText("ERROR") ||
               containsText("Invalid") ||
               containsText("INVALID");
    }
    
    /**
     * Get error messages from screen
     */
    public List<String> getErrorMessages() {
        List<String> errors = new ArrayList<>();
        String[] lines = getLines();
        
        for (String line : lines) {
            if (line.toLowerCase().contains("error") || 
                line.toLowerCase().contains("invalid")) {
                errors.add(line.trim());
            }
        }
        
        return errors;
    }
    
    /**
     * Clear screen and refresh
     */
    public void clear() {
        terminal.clearScreen();
        refresh();
    }
    
    @Override
    public String toString() {
        return "AS400Screen{" +
                "fieldsCount=" + fields.size() +
                ", hasText=" + (screenText != null && !screenText.trim().isEmpty()) +
                '}';
    }
}
