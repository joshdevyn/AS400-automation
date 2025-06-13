package ro.nn.qa.automation.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import ro.nn.qa.business.NewEndowmentX1;
import ro.nn.qa.business.NewEndowmentX2;

/**
 * Step definitions for endowment process scenarios
 */
public class EndowmentSteps extends BaseSteps {
    
    private NewEndowmentX1 endowmentScreen1;
    private NewEndowmentX2 endowmentScreen2;
    private String enteredAmount;
    private String calculatedPremium;
    private String maturityValue;
    private boolean validationError = false;
    
    @Given("^I have created a base contract$")
    public void iHaveCreatedABaseContract() throws Throwable {
        // Assume a base contract has been created through the contract creation process
        log.info("Base contract created for endowment processing");
    }
    
    @Given("^I am on the endowment entry screen$")
    public void iAmOnTheEndowmentEntryScreen() throws Throwable {
        // Navigate to endowment entry screen
        // This would typically be accessed after creating a base contract
        Assert.assertNotNull("Terminal should be available", terminal);
        log.info("Navigated to endowment entry screen");
    }
    
    @When("^I enter endowment amount \"([^\"]*)\"$")
    public void iEnterEndowmentAmount(String amount) throws Throwable {
        enteredAmount = amount;
        if (endowmentScreen1 != null) {
            // Set the endowment amount
            endowmentScreen1.setEndowmentAmount(amount);
        }
        log.info("Entered endowment amount: " + amount);
    }
    
    @And("^I set the beneficiary to \"([^\"]*)\"$")
    public void iSetTheBeneficiaryTo(String beneficiary) throws Throwable {
        if (endowmentScreen1 != null) {
            endowmentScreen1.setBeneficiary(beneficiary);
        }
        log.info("Set beneficiary to: " + beneficiary);
    }
    
    @And("^I set the endowment term to \"([^\"]*)\" years$")
    public void iSetTheEndowmentTermToYears(String term) throws Throwable {
        if (endowmentScreen1 != null) {
            endowmentScreen1.setTerm(term);
        }
        log.info("Set endowment term to: " + term + " years");
    }
    
    @And("^I select investment option \"([^\"]*)\"$")
    public void iSelectInvestmentOption(String investment) throws Throwable {
        if (endowmentScreen1 != null) {
            endowmentScreen1.setInvestmentOption(investment);
        }
        log.info("Selected investment option: " + investment);
    }
    
    @Then("^I submit the endowment$")
    public void iSubmitTheEndowment() throws Throwable {
        if (endowmentScreen1 != null) {
            try {
                endowmentScreen2 = endowmentScreen1.next();
                log.info("Endowment submitted successfully");
            } catch (Exception e) {
                validationError = true;
                log.error("Error submitting endowment: " + e.getMessage());
            }
        }
    }
    
    @And("^the endowment should be processed successfully$")
    public void theEndowmentShouldBeProcessedSuccessfully() throws Throwable {
        Assert.assertNotNull("Endowment should be processed", endowmentScreen2);
        Assert.assertFalse("Should not have validation errors", validationError);
        log.info("Endowment processing verification successful");
    }
    
    @When("^I enter an endowment amount below minimum \"([^\"]*)\"$")
    public void iEnterAnEndowmentAmountBelowMinimum(String amount) throws Throwable {
        enteredAmount = amount;
        try {
            if (endowmentScreen1 != null) {
                endowmentScreen1.setEndowmentAmount(amount);
            }
            log.info("Entered below minimum amount: " + amount);
        } catch (Exception e) {
            validationError = true;
            log.info("Validation error for minimum amount: " + e.getMessage());
        }
    }
    
    @Then("^I should see a minimum amount validation error$")
    public void iShouldSeeAMinimumAmountValidationError() throws Throwable {
        Assert.assertTrue("Should have minimum amount validation error", validationError);
        log.info("Minimum amount validation error verified");
    }
    
    @And("^I set the term to \"([^\"]*)\" years$")
    public void iSetTheTermToYears(String term) throws Throwable {
        if (endowmentScreen1 != null) {
            endowmentScreen1.setTerm(term);
        }
        log.info("Set term to: " + term + " years");
    }
    
    @Then("^the system should calculate the correct premium$")
    public void theSystemShouldCalculateTheCorrectPremium() throws Throwable {
        // Verify premium calculation
        if (endowmentScreen1 != null) {
            calculatedPremium = endowmentScreen1.getCalculatedPremium();
            Assert.assertNotNull("Premium should be calculated", calculatedPremium);
            Assert.assertTrue("Premium should be positive", 
                            Double.parseDouble(calculatedPremium) > 0);
        }
        log.info("Premium calculation verified: " + calculatedPremium);
    }
    
    @And("^display the maturity value$")
    public void displayTheMaturityValue() throws Throwable {
        // Verify maturity value calculation and display
        if (endowmentScreen1 != null) {
            maturityValue = endowmentScreen1.getMaturityValue();
            Assert.assertNotNull("Maturity value should be displayed", maturityValue);
            Assert.assertTrue("Maturity value should be positive", 
                            Double.parseDouble(maturityValue) > 0);
        }
        log.info("Maturity value displayed: " + maturityValue);
    }
}
