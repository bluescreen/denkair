package de.denkair.booking.e2e;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Browser-driven scenarios. Boots headless Chrome once per scenario, drives
 * the real Thymeleaf-rendered pages over the same random-port app that
 * HttpSteps hits.
 *
 * Skips itself if E2E_SKIP_BROWSER=1 or if Chrome / chromedriver isn't resolvable.
 */
public class BrowserSteps {

    @LocalServerPort private int port;
    @Autowired private CucumberSpringConfig springConfig;  // ensures the context is eager-loaded

    private WebDriver driver;
    private WebDriverWait wait;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Before("@browser")
    public void launchBrowser() {
        if ("1".equals(System.getenv("E2E_SKIP_BROWSER"))) {
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "E2E_SKIP_BROWSER=1");
        }
        ChromeOptions opts = new ChromeOptions();
        opts.addArguments("--headless=new", "--no-sandbox", "--disable-gpu",
                "--window-size=1400,900", "--disable-dev-shm-usage",
                "--remote-allow-origins=*");
        String chromePath = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome";
        if (new File(chromePath).canExecute()) {
            opts.setBinary(chromePath);
        }
        try {
            ChromeDriverService.Builder svcBuilder = new ChromeDriverService.Builder()
                    .withLogFile(new File("target/chromedriver.log"))
                    .withVerbose(true);
            File localDriver = findLocalChromedriver();
            if (localDriver != null) {
                svcBuilder.usingDriverExecutable(localDriver);
            }
            driver = new ChromeDriver(svcBuilder.build(), opts);
        } catch (Throwable t) {
            org.junit.jupiter.api.Assumptions.assumeTrue(false,
                    "Chrome/chromedriver not available: " + t.getMessage());
        }
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /** Looks for a cached Selenium-Manager chromedriver under ~/.cache/selenium/. */
    private static File findLocalChromedriver() {
        Path root = Paths.get(System.getProperty("user.home"), ".cache/selenium/chromedriver");
        if (!root.toFile().isDirectory()) return null;
        try (Stream<Path> s = java.nio.file.Files.walk(root)) {
            return s.filter(p -> p.getFileName().toString().equals("chromedriver"))
                    .map(Path::toFile)
                    .filter(File::isFile)
                    .filter(File::canExecute)
                    .findFirst().orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    @After("@browser")
    public void closeBrowser() {
        if (driver != null) driver.quit();
    }

    @Given("I open {string} in a real browser")
    public void iOpen(String path) {
        driver.get(url(path));
    }

    @Then("the page title contains {string}")
    public void titleContains(String needle) {
        wait.until(ExpectedConditions.titleContains(needle));
        assertTrue(driver.getTitle().contains(needle), "title=" + driver.getTitle());
    }

    @Then("the page body contains {string}")
    public void bodyContains(String needle) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        String body = driver.findElement(By.tagName("body")).getText();
        assertTrue(body.contains(needle),
                "body did not contain '" + needle + "' — first 400 chars: "
                        + body.substring(0, Math.min(body.length(), 400)));
    }

    @Then("the page URL contains {string}")
    public void urlContains(String needle) {
        String cur = driver.getCurrentUrl();
        assertTrue(cur.contains(needle), "url=" + cur);
    }

    @When("I click the first link whose href contains {string}")
    public void clickFirstLink(String hrefNeedle) {
        List<WebElement> links = driver.findElements(By.tagName("a"));
        WebElement hit = null;
        for (WebElement a : links) {
            String h = a.getAttribute("href");
            if (h != null && h.contains(hrefNeedle)) { hit = a; break; }
        }
        assertNotNull(hit, "no <a href contains '" + hrefNeedle + "'>");
        hit.click();
    }

    @When("I fill in {string} with {string}")
    public void fillIn(String name, String value) {
        WebElement el = driver.findElement(By.name(name));
        el.clear();
        el.sendKeys(value);
    }

    @When("I submit the booking form")
    public void submitForm() {
        driver.findElement(By.cssSelector("form[action$='/booking'] button[type='submit']")).click();
    }
}
