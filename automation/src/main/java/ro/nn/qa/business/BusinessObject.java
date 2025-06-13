package ro.nn.qa.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.nn.qa.automation.terminal.AS400Terminal;
import ro.nn.qa.automation.terminal.AS400Screen;
import ro.nn.qa.automation.terminal.AS400Field;

import java.util.List;

/**
 * Modern Business Object for AS400 automation
 * Replaces the old TN5250j-based BusinessObjectX
 */
public class BusinessObject {
    protected static final Logger logger = LoggerFactory.getLogger(BusinessObject.class);
    
    protected AS400Terminal terminal;
    protected AS400Screen screen;
    protected final int PAGE_DELAY = 1000;
    protected final int TAB_DELAY = 200;
    protected final int FIELD_DELAY = 500;
    
    /**
     * Constructor
     */
    public BusinessObject() {
        // Default constructor
    }
    
    /**
     * Constructor with terminal
     */
    public BusinessObject(AS400Terminal terminal) {
        this.terminal = terminal;
        this.screen = new AS400Screen(terminal);
    }
    
    /**
     * Set the AS400 terminal
     */
    public void setTerminal(AS400Terminal terminal) {
        this.terminal = terminal;
        this.screen = new AS400Screen(terminal);
    }
    
    /**
     * Get the AS400 terminal
     */
    public AS400Terminal getTerminal() {
        return terminal;
    }
    
    /**
     * Get the current screen
     */
    public AS400Screen getScreen() {
        if (screen == null && terminal != null) {
            screen = new AS400Screen(terminal);
        }
        return screen;
    }
    
    /**
     * Refresh the current screen
     */
    public void refreshScreen() {
        if (screen != null) {
            screen.refresh();
        }
    }
    
    /**
     * Wait for a specific amount of time
     */
    protected void waitFor(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            logger.warn("Wait interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Wait for page to load
     */
    protected void waitForPage() {
        waitFor(PAGE_DELAY);
        refreshScreen();
    }
    
    /**
     * Wait for tab navigation
     */
    protected void waitForTab() {
        waitFor(TAB_DELAY);
    }
    
    /**
     * Wait for field input
     */
    protected void waitForField() {
        waitFor(FIELD_DELAY);
    }
    
    /**
     * Enter text into a field by label
     */
    protected void enterField(String fieldLabel, String value) throws Exception {
        logger.debug("Entering field '{}' with value '{}'", fieldLabel, value);
        
        if (screen == null) {
            throw new IllegalStateException("Screen not initialized");
        }
        
        screen.setFieldValue(fieldLabel, value);
        waitForField();
    }
    
    /**
     * Enter text into a field by index
     */
    protected void enterField(int fieldIndex, String value) throws Exception {
        logger.debug("Entering field {} with value '{}'", fieldIndex, value);
        
        if (screen == null) {
            throw new IllegalStateException("Screen not initialized");
        }
        
        screen.setFieldValue(fieldIndex, value);
        waitForField();
    }
    
    /**
     * Get field value by label
     */
    protected String getFieldValue(String fieldLabel) {
        if (screen == null) {
            return "";
        }
        
        AS400Field field = screen.getFieldByLabel(fieldLabel);
        return field != null ? field.getValueAsString() : "";
    }
    
    /**
     * Get field value by index
     */
    protected String getFieldValue(int fieldIndex) {
        if (screen == null) {
            return "";
        }
        
        AS400Field field = screen.getField(fieldIndex);
        return field != null ? field.getValueAsString() : "";
    }
    
    /**
     * Check if text exists on current screen
     */
    protected boolean isTextOnScreen(String text) {
        return screen != null && screen.containsText(text);
    }
    
    /**
     * Wait for specific text to appear
     */
    protected boolean waitForText(String text, long timeoutMs) throws Exception {
        if (screen == null) {
            return false;
        }
        return screen.waitForText(text, timeoutMs);
    }
    
    /**
     * Press function key
     */
    protected void pressFunctionKey(int functionKey) throws Exception {
        logger.debug("Pressing function key F{}", functionKey);
        
        if (screen != null) {
            screen.pressFunctionKey(functionKey);
        } else if (terminal != null) {
            terminal.sendFunctionKey(functionKey);
        }
        
        waitForPage();
    }
    
    /**
     * Press Enter key
     */
    protected void pressEnter() throws Exception {
        logger.debug("Pressing Enter");
        
        if (screen != null) {
            screen.pressEnter();
        } else if (terminal != null) {
            terminal.pressEnter();
        }
        
        waitForPage();
    }
    
    /**
     * Press F3 (Exit)
     */
    protected void pressF3() throws Exception {
        pressFunctionKey(3);
    }
      /**
     * Press F4 (Prompt/Help)
     */
    protected void pressF4() throws Exception {
        pressFunctionKey(4);
    }
    
    /**
     * Press F5 (Refresh/Continue)
     */
    protected void pressF5() throws Exception {
        pressFunctionKey(5);
    }
    
    /**
     * Press F12 (Cancel)
     */
    protected void pressF12() throws Exception {
        pressFunctionKey(12);
    }
    
    /**
     * Navigate to a menu option
     */
    protected void navigateToMenu(String menuOption) throws Exception {
        logger.info("Navigating to menu option: {}", menuOption);
        
        if (terminal != null) {
            terminal.navigateToMenu(menuOption);
            waitForPage();
        }
    }
    
    /**
     * Check if current screen has errors
     */
    protected boolean hasErrors() {
        return screen != null && screen.hasErrors();
    }
    
    /**
     * Get error messages from current screen
     */
    protected List<String> getErrorMessages() {
        if (screen != null) {
            return screen.getErrorMessages();
        }
        return List.of();
    }
    
    /**
     * Validate that we're on the expected screen
     */
    protected boolean validateScreen(String expectedText) {
        boolean isValid = isTextOnScreen(expectedText);
        if (!isValid) {
            logger.warn("Screen validation failed. Expected text '{}' not found", expectedText);
            logger.debug("Current screen: {}", screen != null ? screen.getText() : "null");
        }
        return isValid;
    }
    
    /**
     * Get all fields on current screen
     */
    protected List<AS400Field> getAllFields() {
        return screen != null ? screen.getFields() : List.of();
    }
    
    /**
     * Get current screen text
     */
    protected String getScreenText() {
        return screen != null ? screen.getText() : "";
    }
    
    /**
     * Clear screen
     */
    protected void clearScreen() {
        if (screen != null) {
            screen.clear();
        }
    }
    
    /**
     * Check if we're on the main menu
     */
    protected boolean isMainMenu() {
        return screen != null && screen.isMainMenu();
    }
    
    /**
     * Return to main menu (typically F3 multiple times)
     */
    protected void returnToMainMenu() throws Exception {
        logger.info("Returning to main menu");
        
        int attempts = 0;
        int maxAttempts = 5;
        
        while (!isMainMenu() && attempts < maxAttempts) {
            pressF3();
            attempts++;
            
            if (hasErrors()) {
                logger.warn("Error detected while returning to main menu: {}", getErrorMessages());
                break;
            }
        }
        
        if (!isMainMenu()) {
            logger.warn("Could not return to main menu after {} attempts", maxAttempts);
        } else {
            logger.info("Successfully returned to main menu");
        }
    }
    
    /**
     * Handle F4 help/selection
     */
    protected void handleF4Selection(String selectionValue) throws Exception {
        logger.debug("Handling F4 selection: {}", selectionValue);
        
        pressF4();
        waitForPage();
        
        if (screen != null && screen.isSelectionList()) {
            // Enter selection value
            terminal.sendText(selectionValue);
            pressEnter();
            waitForPage();
        } else {
            logger.warn("F4 did not open selection list");
        }
    }
    
    /**
     * Generic method to fill form fields
     */
    protected void fillForm(java.util.Map<String, String> fieldValues) throws Exception {
        logger.info("Filling form with {} fields", fieldValues.size());
        
        for (java.util.Map.Entry<String, String> entry : fieldValues.entrySet()) {
            String fieldLabel = entry.getKey();
            String value = entry.getValue();
            
            if (value != null && !value.trim().isEmpty()) {
                enterField(fieldLabel, value);
            }
        }
        
        logger.info("Form filling completed");
    }
    
    /**
     * Validate form submission
     */
    protected boolean validateFormSubmission() {
        if (hasErrors()) {
            List<String> errors = getErrorMessages();
            logger.error("Form submission failed with errors: {}", errors);
            return false;
        }
        
        // Additional validation can be added here
        return true;
    }
}
