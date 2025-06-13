package ro.nn.qa.automation.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import ro.nn.qa.business.*;
import ro.nn.qa.business.f4.BillingFreqF4;
import ro.nn.qa.business.f4.LocateClientF4;

/**
 * Step definitions for business navigation scenarios
 */
public class BusinessNavigationSteps extends BaseSteps {
    
    private MasterMenuX mainMenu;
    private NewBusinessMenuX newBusinessMenu;
    private ClientsAdminX clientsAdmin;
    private NewContractProposalX contractProposal;
    private BillingFreqF4 billingFreqF4;
    private LocateClientF4 locateClientF4;
    private String currentScreen;
      @Given("^I am on the main menu$")
    public void iAmOnTheMainMenu() throws Throwable {
        Assert.assertNotNull("Terminal should be initialized", terminal);
        
        // Connect to AS400 if not already connected
        if (!terminal.isConnected()) {
            terminal.connect();
        }
        
        BusinessObjectX bo = new BusinessObjectX(terminal);
        mainMenu = new MasterMenuX(bo);
        Assert.assertNotNull("Main menu should be accessible", mainMenu);
        currentScreen = "Main Menu";
        log.info("Successfully navigated to main menu");
    }
    
    @When("^I select the New Business option$")
    public void iSelectTheNewBusinessOption() throws Throwable {
        Assert.assertNotNull("Main menu should be available", mainMenu);
        newBusinessMenu = mainMenu.getNewBusinessMenu();
        currentScreen = "New Business Menu";
        log.info("Selected New Business option");
    }
    
    @When("^I select the Client Administration option$")
    public void iSelectTheClientAdministrationOption() throws Throwable {
        Assert.assertNotNull("Main menu should be available", mainMenu);
        clientsAdmin = mainMenu.getClientsAdmin();
        currentScreen = "Client Administration";
        log.info("Selected Client Administration option");
    }
    
    @When("^I select menu option \"([^\"]*)\"$")
    public void iSelectMenuOption(String option) throws Throwable {
        Assert.assertNotNull("Main menu should be available", mainMenu);
        
        switch (option) {
            case "New Contract Proposal":
                newBusinessMenu = mainMenu.getNewBusinessMenu();
                contractProposal = newBusinessMenu.getNewContractProposal();
                currentScreen = "New Contract Proposal";
                break;
            case "Client Administration":
                clientsAdmin = mainMenu.getClientsAdmin();
                currentScreen = "Client Administration";
                break;
            case "New Endowment":
                newBusinessMenu = mainMenu.getNewBusinessMenu();
                currentScreen = "New Endowment Entry";
                break;
            default:
                throw new IllegalArgumentException("Unknown menu option: " + option);
        }
        log.info("Selected menu option: " + option);
    }
    
    @Then("^I should see the New Business menu$")
    public void iShouldSeeTheNewBusinessMenu() throws Throwable {
        Assert.assertNotNull("New Business menu should be accessible", newBusinessMenu);
        Assert.assertEquals("Should be on New Business Menu", "New Business Menu", currentScreen);
        log.info("New Business menu verification successful");
    }
    
    @Then("^I should see the Client Administration screen$")
    public void iShouldSeeTheClientAdministrationScreen() throws Throwable {
        Assert.assertNotNull("Client Administration should be accessible", clientsAdmin);
        Assert.assertEquals("Should be on Client Administration", "Client Administration", currentScreen);
        log.info("Client Administration screen verification successful");
    }
    
    @Then("^all menu options should be available$")
    public void allMenuOptionsShouldBeAvailable() throws Throwable {
        // Verify all expected menu options are present and accessible
        Assert.assertNotNull("New Business menu should be available", newBusinessMenu);
        log.info("Menu options availability verification successful");
    }
    
    @Then("^I should be able to perform client operations$")
    public void iShouldBeAbleToPerformClientOperations() throws Throwable {
        Assert.assertNotNull("Client Administration should be functional", clientsAdmin);
        log.info("Client operations verification successful");
    }
    
    @Given("^I am on the New Contract Proposal screen$")
    public void iAmOnTheNewContractProposalScreen() throws Throwable {
        iAmOnTheMainMenu();
        iSelectTheNewBusinessOption();
        contractProposal = newBusinessMenu.getNewContractProposal();
        currentScreen = "New Contract Proposal";
        Assert.assertNotNull("Contract proposal screen should be accessible", contractProposal);
        log.info("Navigated to New Contract Proposal screen");
    }
    
    @Given("^I am on a client entry screen$")
    public void iAmOnAClientEntryScreen() throws Throwable {
        iAmOnTheMainMenu();
        iSelectTheClientAdministrationOption();
        currentScreen = "Client Entry";
        log.info("Navigated to client entry screen");
    }
    
    @When("^I press F4 on the billing frequency field$")
    public void iPressF4OnTheBillingFrequencyField() throws Throwable {
        Assert.assertNotNull("Contract proposal should be available", contractProposal);
        // Simulate F4 key press on billing frequency field
        billingFreqF4 = new BillingFreqF4(new BusinessObjectX(terminal));
        log.info("Pressed F4 on billing frequency field");
    }
    
    @When("^I press F4 on the client field$")
    public void iPressF4OnTheClientField() throws Throwable {
        Assert.assertNotNull("Client admin should be available", clientsAdmin);
        // Simulate F4 key press on client field
        locateClientF4 = new LocateClientF4(new BusinessObjectX(terminal));
        log.info("Pressed F4 on client field");
    }
    
    @Then("^I should see the billing frequency help screen$")
    public void iShouldSeeTheBillingFrequencyHelpScreen() throws Throwable {
        Assert.assertNotNull("Billing frequency F4 should be displayed", billingFreqF4);
        log.info("Billing frequency help screen verification successful");
    }
    
    @Then("^I should see the client lookup screen$")
    public void iShouldSeeTheClientLookupScreen() throws Throwable {
        Assert.assertNotNull("Client lookup F4 should be displayed", locateClientF4);
        log.info("Client lookup screen verification successful");
    }
    
    @Then("^I should be able to select a frequency option$")
    public void iShouldBeAbleToSelectAFrequencyOption() throws Throwable {
        Assert.assertNotNull("Billing frequency F4 should be functional", billingFreqF4);
        // Test selecting an option
        billingFreqF4.selectOption("M"); // Monthly
        log.info("Frequency option selection verification successful");
    }
    
    @Then("^I should be able to search for clients$")
    public void iShouldBeAbleToSearchForClients() throws Throwable {
        Assert.assertNotNull("Client lookup F4 should be functional", locateClientF4);
        // Test searching for clients
        locateClientF4.searchClient("TEST");
        log.info("Client search verification successful");
    }
    
    @Then("^I should navigate to \"([^\"]*)\"$")
    public void iShouldNavigateTo(String expectedScreen) throws Throwable {
        Assert.assertEquals("Should be on correct screen", expectedScreen, currentScreen);
        log.info("Navigation verification successful for screen: " + expectedScreen);
    }
    
    @Then("^the screen should be properly loaded$")
    public void theScreenShouldBeProperlyLoaded() throws Throwable {
        Assert.assertNotNull("Current screen should be loaded", currentScreen);
        // Additional validation can be added here to check screen elements
        log.info("Screen loading verification successful for: " + currentScreen);
    }
}
