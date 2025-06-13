package ro.nn.qa.automation.steps;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.nn.qa.automation.terminal.AS400Terminal;
import ro.nn.qa.bootstrap.Controller;

/**
 * Created by Alexandru Giurovici on 14.09.2015.
 * Updated to use modern AS400Terminal and SLF4J logging
 */

@RunWith(Cucumber.class)
@CucumberOptions(
        // plugin = {"pretty", "html:target/cucumber"},
        features="src/test/resources"
)
public class BaseSteps
{
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    protected Controller controller;
    protected AS400Terminal terminal;
}
