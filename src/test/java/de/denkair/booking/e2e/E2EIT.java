package de.denkair.booking.e2e;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Cucumber runner for Gherkin-style E2E tests.
 * Picked up by maven-failsafe-plugin via the *IT.java pattern.
 * Invoke with: make e2e   (or: mvn verify -DskipUnitTests)
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "de.denkair.booking.e2e",
        plugin = { "pretty", "summary", "html:target/cucumber-report.html" }
)
public class E2EIT {
}
