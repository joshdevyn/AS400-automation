package ro.nn.qa.automation.tests;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Regression test runner for comprehensive AS400 automation testing
 */
@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources",
    glue = "ro.nn.qa.automation.steps",    plugin = {
        "pretty",
        "html:target/regression-reports/html",
        "json:target/regression-reports/cucumber.json",
        "junit:target/regression-reports/cucumber.xml"
    },
    tags = "not @ignore"
)
public class RegressionTestRunner {
    // Regression test configuration
}
