package ro.nn.qa.automation.steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StepsRunner
{
    private static final Logger log = LoggerFactory.getLogger(StepsRunner.class);
    
    // Common setup and teardown for all step classes
    
    @Before
    public void setUp() throws Throwable {
        log.info("Setting up test scenario");
        // Common setup logic can go here
    }    @After
    public void tearDown() throws Throwable {
        log.info("Cleaning up test scenario");
        // Individual step classes will handle their own cleanup
        // The singleton Controller will be shared across tests
    }
}
