package ro.nn.qa.automation.tests;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Smoke test runner for critical AS400 automation scenarios
 */
@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources",
    glue = "ro.nn.qa.automation.steps",    plugin = {
        "pretty",
        "html:target/smoke-reports/html",
        "json:target/smoke-reports/cucumber.json"
    },
    tags = "@smoke"
)
public class SmokeTestRunner {
    // Smoke test configuration
}
