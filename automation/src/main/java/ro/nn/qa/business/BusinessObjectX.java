package ro.nn.qa.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.nn.qa.automation.terminal.AS400Terminal;
import ro.nn.qa.automation.terminal.AS400Screen;
import ro.nn.qa.automation.terminal.AS400Field;

import java.util.List;

/**
 * Legacy BusinessObjectX - Updated to use modern AS400Terminal
 * Created by Alexandru Giurovici on 18.09.2015.
 * Modernized to use JSch-based AS400Terminal instead of TN5250j
 */
public class BusinessObjectX extends BusinessObject {
    private static final Logger log = LoggerFactory.getLogger(BusinessObjectX.class);
    
    /**
     * Default constructor
     */
    public BusinessObjectX() {
        super();
    }
    
    /**
     * Constructor with terminal
     */
    public BusinessObjectX(AS400Terminal terminal) {
        super(terminal);
    }
    
    /**
     * Legacy method - get current field (modernized)
     */
    protected AS400Field getCurrentField() {
        if (screen == null) {
            return null;
        }
        
        List<AS400Field> fields = screen.getFields();
        return fields.isEmpty() ? null : fields.get(0);
    }
      /**
     * Legacy method - tab to next field
     */
    protected void tab(int numTabs) throws InterruptedException {
        log.debug("Tabbing {} times", numTabs);
        
        try {
            for (int i = 0; i < numTabs; i++) {
                if (terminal != null) {
                    terminal.sendText("\t");
                    waitForTab();
                }
            }
            waitForPage();
        } catch (Exception e) {
            log.error("Error during tab operation", e);
            throw new InterruptedException("Tab operation failed: " + e.getMessage());
        }
    }
    
    /**
     * Legacy method - enter key
     */
    protected void enter() throws InterruptedException {
        log.debug("Pressing Enter");
        try {
            pressEnter();
        } catch (Exception e) {
            log.error("Error pressing Enter", e);
            throw new InterruptedException("Enter operation failed: " + e.getMessage());
        }
    }
    
    /**
     * Legacy method - send text to current field
     */    protected void send(String text) throws InterruptedException {
        log.debug("Sending text: {}", text);
        
        try {
            if (terminal != null) {
                terminal.sendText(text);
                waitForField();
            }
        } catch (Exception e) {
            log.error("Error sending text", e);
            throw new InterruptedException("Send text operation failed: " + e.getMessage());
        }
    }
      /**
     * Legacy method - send text with delay
     */
    protected void send(String text, int delayFactor) throws InterruptedException {
        send(text);
        waitFor(delayFactor * 100); // Convert delay factor to milliseconds
    }
    
    /**
     * Legacy method - check if specific text is on screen
     */
    protected boolean isTextOnScreen(String text) {
        return super.isTextOnScreen(text);
    }
    
    /**
     * Legacy method - get screen fields (modernized)
     */
    protected List<AS400Field> getScreenFields() {
        return getAllFields();
    }
    
    /**
     * Legacy method - type text into field
     */
    protected void type(String text) throws Exception {
        send(text);
    }
    
    /**
     * Legacy method - set field value
     */
    protected void setField(String fieldLabel, String value) throws Exception {
        enterField(fieldLabel, value);
    }
    
    /**
     * Legacy method - set field value by index
     */
    protected void setField(int fieldIndex, String value) throws Exception {
        enterField(fieldIndex, value);
    }
    
    /**
     * Legacy method - wait for screen
     */
    protected void waitForScreen() {
        waitForPage();
    }
    
    /**
     * Legacy method - refresh screen
     */
    protected void refresh() {
        refreshScreen();
    }
    
    /**
     * Legacy method - clear field
     */
    protected void clearField(String fieldLabel) throws Exception {
        enterField(fieldLabel, "");
    }
    
    /**
     * Legacy method - clear field by index
     */
    protected void clearField(int fieldIndex) throws Exception {
        enterField(fieldIndex, "");
    }
    
    /**
     * Legacy method - get field text
     */
    protected String getFieldText(String fieldLabel) {
        return getFieldValue(fieldLabel);
    }
    
    /**
     * Legacy method - get field text by index
     */
    protected String getFieldText(int fieldIndex) {
        return getFieldValue(fieldIndex);
    }
    
    /**
     * Legacy method - check if field exists
     */
    protected boolean fieldExists(String fieldLabel) {
        if (screen == null) {
            return false;
        }
        
        AS400Field field = screen.getFieldByLabel(fieldLabel);
        return field != null;
    }
    
    /**
     * Legacy method - navigation helper
     */
    protected void gotoMenu(String menuOption) throws Exception {
        navigateToMenu(menuOption);
    }
    
    /**
     * Legacy method - error checking
     */
    protected boolean hasError() {
        return hasErrors();
    }
    
    /**
     * Legacy method - get error text
     */
    protected String getErrorText() {
        List<String> errors = getErrorMessages();
        return errors.isEmpty() ? "" : String.join("; ", errors);
    }
      /**
     * Legacy method - function key support
     */
    protected void functionKey(int keyNumber) throws Exception {
        pressFunctionKey(keyNumber);
    }
    
    /**
     * Legacy method - F3 exit
     */
    protected void f3() throws InterruptedException {
        try {
            pressF3();
        } catch (Exception e) {
            log.error("Error pressing F3", e);
            throw new InterruptedException("F3 operation failed: " + e.getMessage());
        }
    }
    
    /**
     * Legacy method - F4 help
     */
    protected void f4() throws InterruptedException {
        try {
            pressF4();
        } catch (Exception e) {
            log.error("Error pressing F4", e);
            throw new InterruptedException("F4 operation failed: " + e.getMessage());
        }
    }
    
    /**
     * Legacy method - F5 refresh/continue
     */
    protected void f5() throws InterruptedException {
        try {
            pressF5();
        } catch (Exception e) {
            log.error("Error pressing F5", e);
            throw new InterruptedException("F5 operation failed: " + e.getMessage());
        }
    }
    
    /**
     * Legacy method - F12 cancel
     */
    protected void f12() throws InterruptedException {
        try {
            pressF12();
        } catch (Exception e) {
            log.error("Error pressing F12", e);
            throw new InterruptedException("F12 operation failed: " + e.getMessage());
        }
    }
    
    /**
     * Legacy method - erase current field
     */
    protected void erasefld() throws InterruptedException {
        log.debug("Erasing current field");
        
        try {
            if (terminal != null) {
                // Clear the current field by sending spaces or using field clear command
                terminal.sendText(""); // Clear current field
                waitForField();
            }
        } catch (Exception e) {
            log.error("Error erasing field", e);
            throw new InterruptedException("Erase field operation failed: " + e.getMessage());
        }
    }
    
    /**
     * Legacy method - wait for specific text
     */
    protected boolean waitForText(String text) throws Exception {
        return waitForText(text, 10000); // 10 second default timeout
    }
    
    /**
     * Legacy method - wait for specific text with timeout
     */
    protected boolean waitForText(String text, long timeoutMs) throws Exception {
        return super.waitForText(text, timeoutMs);
    }
    
    /**
     * Legacy method - screen validation
     */
    protected boolean isOnScreen(String screenIdentifier) {
        return validateScreen(screenIdentifier);
    }
    
    /**
     * Legacy method - get all screen text
     */
    protected String getAllText() {
        return getScreenText();
    }
    
    /**
     * Legacy method - position to field
     */
    protected void positionTo(String fieldLabel) throws Exception {
        if (screen != null) {
            AS400Field field = screen.getFieldByLabel(fieldLabel);
            if (field != null) {
                // Field will be automatically positioned when setting value
                log.debug("Positioned to field: {}", fieldLabel);
            } else {
                log.warn("Field not found: {}", fieldLabel);
            }
        }
    }
    
    /**
     * Legacy method - check if on main menu
     */
    protected boolean isOnMainMenu() {
        return isMainMenu();
    }
    
    /**
     * Legacy method - return to main menu
     */
    protected void goToMainMenu() throws Exception {
        returnToMainMenu();
    }
    
    /**
     * Legacy method - get field value by index (0-based)
     */
    protected String getField(int fieldIndex) {
        if (screen == null) {
            return "";
        }
        
        List<AS400Field> fields = screen.getFields();
        if (fieldIndex >= 0 && fieldIndex < fields.size()) {
            return fields.get(fieldIndex).getValue();
        }
        
        log.warn("Field index {} out of range (0-{})", fieldIndex, fields.size() - 1);
        return "";    }
}
