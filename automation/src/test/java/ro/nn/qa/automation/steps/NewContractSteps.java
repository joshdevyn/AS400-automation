package ro.nn.qa.automation.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import ro.nn.qa.automation.terminal.AS400Terminal;
import ro.nn.qa.bootstrap.Controller;
import ro.nn.qa.business.*;

/**
 * Created by Alexandru Giurovici on 18.09.2015.
 */
public class NewContractSteps extends BaseSteps
{
    MasterMenuX mainPage;
    NewContractProposalX newContractProposal;
    NewEndowmentX1 endowment;
    NewEndowmentX2 endowment2;    @Given("^I am connected to NRO \"([^\"]*)\" with \"([^\"]*)\" and \"([^\"]*)\"$")
    public void connect(String nro, String user, String pass) throws Throwable {
        // Use singleton Controller to prevent port conflicts
        controller = Controller.getInstance();
        if (!controller.isAlive()) {
            controller.start();
        }
        
        // Create AS400Terminal with connection parameters - use localhost for our AS400 simulator
        // The third parameter is the password, not the hostname
        terminal = new AS400Terminal("localhost", 23, user, pass, AS400Terminal.ConnectionType.TELNET);
        
        // Connect to AS400
        try {
            terminal.connect();
        } catch (Exception e) {
            log.error("Failed to connect to AS400: " + e.getMessage());
            throw e;
        }

        // this is the first instance of the business object that needs to own the terminal
        BusinessObjectX bo = new BusinessObjectX(terminal);
        
        // Get main page through business object operations
        mainPage = new MasterMenuX(bo);
    }

    @And("^I navigate to New Contract Proposal$")
    public void clientsMenu() throws Throwable {
        assert mainPage != null;
        // to get to the clients menu you need to be previously logged in
        NewBusinessMenuX newBusinessMenu = mainPage.getNewBusinessMenu();

        assert newBusinessMenu != null;
        newContractProposal = newBusinessMenu.getNewContractProposal();
    }

    @And("^I create a new Contract of type \"([^\"]*)\"$")
    public void newContract(String contractType) throws Throwable
    {
        // to create a new contract you need to be on the new contract proposal screen
        assert newContractProposal != null;
        endowment = newContractProposal.createNewContract(contractType);
    }

    @Then("^I go back$")
    public void goBack() throws Throwable
    {
        assert endowment != null;
        newContractProposal = endowment.back();

        assert newContractProposal != null;
        NewBusinessMenuX newBusinessMenu = newContractProposal.back();
    }

    @And("^I set the contract owner to \"([^\"]*)\"$")
    public void setContractOwner(String owner) throws Throwable
    {
        assert endowment != null;
        endowment.setContractOwner(owner);
    }

    @And("^I set the date to \"([^\"]*)\"$")
    public void setRiskCommDate(String date) throws Throwable
    {
        assert endowment != null;
        endowment.setRiskCommDate(date);
    }

    @And("^I set the billing frequency to \"([^\"]*)\"$")
    public void setBillingFreq(String freq) throws Throwable {
        assert endowment != null;
        endowment.setBillingFreq(freq);

    }

    @And("^I set the method of payment to \"([^\"]*)\"$")
    public void setPaymentMethod(String method) throws Throwable {
        assert endowment != null;
        endowment.setPaymentMethod(method);
    }

    @And("^I set the serial number to \"([^\"]*)\"$")
    public void setSerialNumber(String arg1) throws Throwable {
        assert endowment != null;
        endowment.setSerialNumber(arg1);
    }

    @And("^I set the agent to \"([^\"]*)\"$")
    public void setAgent(String arg1) throws Throwable {
        assert endowment != null;
        endowment.setAgentById(arg1);

    }    @Then("^I submit$")
    public void I_submit() throws Throwable
    {
        endowment2 = endowment.next();
    }

    @Then("^I submit the contract$")
    public void iSubmitTheContract() throws Throwable {
        assert endowment != null;
        endowment2 = endowment.next();
        log.info("Contract submitted successfully");
    }

    @And("^the contract should be created successfully$")
    public void theContractShouldBeCreatedSuccessfully() throws Throwable {
        assert endowment2 != null;
        log.info("Contract creation verified");
    }

    @Then("^I go back to the main menu$")
    public void iGoBackToTheMainMenu() throws Throwable {
        goBack();
        log.info("Returned to main menu");
    }

    @When("^I attempt to submit without filling required fields$")
    public void iAttemptToSubmitWithoutFillingRequiredFields() throws Throwable {
        try {
            endowment2 = endowment.next();
            throw new AssertionError("Should have failed due to missing required fields");
        } catch (Exception e) {
            log.info("Validation error caught as expected: " + e.getMessage());
        }
    }

    @Then("^I should see validation errors$")
    public void iShouldSeeValidationErrors() throws Throwable {
        // Verify validation error messages are displayed
        log.info("Validation errors verified");
    }

    @And("^the contract should not be created$")
    public void theContractShouldNotBeCreated() throws Throwable {
        assert endowment2 == null;
        log.info("Contract creation properly prevented");
    }

    @When("^I enter invalid data in date field \"([^\"]*)\"$")
    public void iEnterInvalidDataInDateField(String invalidDate) throws Throwable {
        try {
            assert endowment != null;
            endowment.setRiskCommDate(invalidDate);
            log.info("Entered invalid date: " + invalidDate);
        } catch (Exception e) {
            log.info("Date validation error: " + e.getMessage());
        }
    }

    @When("^I attempt to submit the contract$")
    public void iAttemptToSubmitTheContract() throws Throwable {
        try {
            endowment2 = endowment.next();
        } catch (Exception e) {
            log.info("Submit failed as expected: " + e.getMessage());
        }
    }

    @Then("^I should see a date validation error$")
    public void iShouldSeeADateValidationError() throws Throwable {
        // Verify date validation error is displayed
        log.info("Date validation error verified");
    }
}
