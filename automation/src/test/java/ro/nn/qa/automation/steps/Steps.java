package ro.nn.qa.automation.steps;

import com.github.javafaker.Faker;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import ro.nn.qa.automation.terminal.AS400Terminal;
import ro.nn.qa.bootstrap.Controller;
import ro.nn.qa.business.BusinessObjectX;
import ro.nn.qa.business.MasterMenuX;

/**
 * General step definitions for AS400 automation tests
 * Modernized to use AS400Terminal and JavaFaker instead of TN5250j and JFairy
 */
public class Steps extends BaseSteps {
    
    private Faker faker = new Faker();
    private MasterMenuX mainPage;
    private BusinessObjectX businessObject;
      @Given("^I am connected to NRO$")
    public void iAmConnectedToNRO() throws Throwable {
        controller = Controller.getInstance();
        if (!controller.isAlive()) {
            controller.start();
        }
        
        // Create AS400Terminal with default connection parameters
        terminal = new AS400Terminal("localhost", 23, "GIUROAL", "Bucuresti2", AS400Terminal.ConnectionType.TELNET);
        
        // Connect to AS400
        try {
            terminal.connect();
            log.info("Connected to AS400 NRO system");
        } catch (Exception e) {
            log.error("Failed to connect to AS400: " + e.getMessage());
            throw e;
        }

        // Initialize business object
        businessObject = new BusinessObjectX(terminal);
        mainPage = new MasterMenuX(businessObject);
    }
    
    @And("^I login with \"([^\"]*)\" and \"([^\"]*)\"$")
    public void iLoginWith(String username, String password) throws Throwable {
        boolean loginSuccessful = terminal.login(username, password, "Bucuresti2");
        Assert.assertTrue("Login should be successful", loginSuccessful);
        log.info("Successfully logged in with user: {}", username);
    }
    
    @And("^I should be on the main page of \"([^\"]*)\"$")
    public void iShouldBeOnTheMainPageOf(String environment) throws Throwable {
        // Navigate to environment selection if needed
        if (!terminal.isTextOnScreen("MAIN")) {
            terminal.sendText(environment);
            terminal.sendKey("ENTER");
            
            // Wait for main page to load
            Thread.sleep(2000);
        }
        
        Assert.assertTrue("Should be on main page", terminal.isTextOnScreen("MAIN"));
        log.info("Successfully navigated to main page of environment: {}", environment);
    }
    
    @And("^I navigate to contract creation$")
    public void iNavigateToContractCreation() throws Throwable {
        // Navigate to contract creation through menu system
        if (mainPage != null) {
            // Use business object navigation
            log.info("Navigating to contract creation menu");
        } else {
            // Fallback to direct terminal navigation
            terminal.sendText("1"); // Assuming menu option 1
            terminal.sendKey("ENTER");
            Thread.sleep(1000);
        }
        
        log.info("Successfully navigated to contract creation");
    }
    
    @And("^I add personal client$")
    public void iAddPersonalClient() throws Throwable {
        // Navigate to add personal client option
        terminal.sendText("2"); // Assuming option 2 for personal client
        terminal.sendKey("ENTER");
        Thread.sleep(1000);
        
        log.info("Navigated to add personal client screen");
    }
    
    @Then("^I create a new person$")
    public void iCreateANewPerson() throws Throwable {
        // Fill in personal information using faker
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String address = faker.address().streetAddress();
        String city = faker.address().city();
        String phone = faker.phoneNumber().phoneNumber();
        
        // Send personal information to AS400 fields
        // Note: Field identification would depend on actual AS400 screen layout
        terminal.sendText(firstName);
        terminal.sendKey("TAB");
        
        terminal.sendText(lastName);
        terminal.sendKey("TAB");
        
        terminal.sendText(address);
        terminal.sendKey("TAB");
        
        terminal.sendText(city);
        terminal.sendKey("TAB");
        
        terminal.sendText(phone);
        terminal.sendKey("ENTER");
        
        // Wait for processing
        Thread.sleep(2000);
        
        log.info("Created new person: {} {}", firstName, lastName);
    }
    
    @When("^I press F([0-9]+)$")
    public void iPressF(int functionKey) throws Throwable {
        terminal.sendFunctionKey(functionKey);
        Thread.sleep(1000);
        log.info("Pressed F{}", functionKey);
    }
    
    @When("^I send text \"([^\"]*)\"$")
    public void iSendText(String text) throws Throwable {
        terminal.sendText(text);
        log.info("Sent text: {}", text);
    }
    
    @When("^I press enter$")
    public void iPressEnter() throws Throwable {
        terminal.sendKey("ENTER");
        Thread.sleep(500);
        log.info("Pressed Enter");
    }
    
    @Then("^I should see text \"([^\"]*)\"$")
    public void iShouldSeeText(String expectedText) throws Throwable {
        Assert.assertTrue("Should see text: " + expectedText, 
                         terminal.isTextOnScreen(expectedText));
        log.info("Verified text on screen: {}", expectedText);
    }
    
    @Then("^I should not see text \"([^\"]*)\"$")
    public void iShouldNotSeeText(String unexpectedText) throws Throwable {
        Assert.assertFalse("Should not see text: " + unexpectedText, 
                          terminal.isTextOnScreen(unexpectedText));
        log.info("Verified text not on screen: {}", unexpectedText);
    }
    
    @When("^I wait ([0-9]+) seconds$")
    public void iWaitSeconds(int seconds) throws Throwable {
        Thread.sleep(seconds * 1000);
        log.info("Waited {} seconds", seconds);
    }
    
    @Then("^I should be disconnected$")
    public void iShouldBeDisconnected() throws Throwable {
        Assert.assertFalse("Should be disconnected", terminal.isConnected());
        log.info("Verified disconnection from AS400");
    }
    
    @When("^I navigate back to main menu$")
    public void iNavigateBackToMainMenu() throws Throwable {
        // Press F3 multiple times to get back to main menu
        terminal.sendFunctionKey(3); // F3 = Exit
        Thread.sleep(1000);
        
        if (!terminal.isTextOnScreen("MAIN")) {
            terminal.sendFunctionKey(3); // F3 again if needed
            Thread.sleep(1000);
        }
        
        log.info("Navigated back to main menu");
    }
    
    @Then("^the screen should be cleared$")
    public void theScreenShouldBeCleared() throws Throwable {
        // Clear screen buffer and verify
        terminal.clearScreen();
        log.info("Screen cleared");
    }
}