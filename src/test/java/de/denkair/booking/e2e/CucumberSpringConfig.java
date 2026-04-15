package de.denkair.booking.e2e;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Boots the real Spring application on a random port for the step definitions
 * to hit over HTTP. One class per test run, shared across all scenarios.
 *
 * Mail is redirected to an in-process GreenMail SMTP (port 3025) so scenarios
 * can assert on the actual email bodies that MailService sends.
 */
@CucumberContextConfiguration
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.banner-mode=off",
                "denkair.log.dir=target/test-logs",
                "spring.mail.host=127.0.0.1",
                "spring.mail.port=3025",
                "spring.mail.username=",
                "spring.mail.password=",
                "spring.mail.properties.mail.smtp.auth=false",
                "spring.mail.properties.mail.smtp.starttls.enable=false"
        }
)
public class CucumberSpringConfig {

    @TestConfiguration
    static class GreenMailConfig {
        /** Shared GreenMail instance — bean lifecycle tied to Spring context. */
        @Bean(initMethod = "start", destroyMethod = "stop")
        public GreenMail greenMail() {
            // ServerSetupTest.SMTP = 127.0.0.1:3025, no auth, no TLS.
            return new GreenMail(ServerSetupTest.SMTP);
        }
    }
}
