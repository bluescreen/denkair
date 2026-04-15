package de.denkair.booking.e2e;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Boots the real Spring application on a random port for the step definitions
 * to hit over HTTP. One class per test run, shared across all scenarios.
 */
@CucumberContextConfiguration
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.banner-mode=off",
                "denkair.log.dir=target/test-logs"
        }
)
public class CucumberSpringConfig {
}
