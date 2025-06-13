package ro.nn.qa.automation.tests;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Comprehensive test runner for all AS400 automation features
 */
@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources",
    glue = "ro.nn.qa.automation.steps",
    plugin = {
        "pretty",
        "html:target/cucumber-reports/html",
        "json:target/cucumber-reports/cucumber.json",
        "junit:target/cucumber-reports/cucumber.xml"
    },
    tags = "@smoke or @authentication or @navigation or @contract or @endowment"
)
public class AS400TestRunner {
    // Test runner configuration
}
