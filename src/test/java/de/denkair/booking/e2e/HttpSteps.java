package de.denkair.booking.e2e;

import de.denkair.booking.domain.Flight;
import de.denkair.booking.repository.FlightRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.HttpURLConnection;
import java.util.Comparator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpSteps {

    @LocalServerPort private int port;
    @Autowired private FlightRepository flightRepository;

    private final RestTemplate rest = new RestTemplateBuilder()
            .requestFactory(() -> {
                SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory() {
                    @Override
                    protected void prepareConnection(HttpURLConnection c, String httpMethod)
                            throws java.io.IOException {
                        super.prepareConnection(c, httpMethod);
                        c.setInstanceFollowRedirects(false);
                    }
                };
                return f;
            })
            .errorHandler(new org.springframework.web.client.DefaultResponseErrorHandler() {
                @Override public boolean hasError(org.springframework.http.client.ClientHttpResponse r) { return false; }
            })
            .build();

    private ResponseEntity<String> lastResponse;
    private Long flightIdUnderTest;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Given("I pick a seeded active flight")
    public void pickSeededFlight() {
        Flight f = flightRepository.findByAktivTrueOrderByDepartureAsc().stream()
                .min(Comparator.comparing(Flight::getDeparture))
                .orElseThrow(() -> new IllegalStateException("no seeded flights"));
        flightIdUnderTest = f.getId();
    }

    @When("I GET {string}")
    public void iGet(String path) {
        lastResponse = rest.getForEntity(url(path), String.class);
    }

    @When("I GET without following redirects {string}")
    public void iGetNoRedirect(String path) {
        lastResponse = rest.getForEntity(url(path), String.class);
    }

    @When("I GET {string} with header {string} = {string}")
    public void iGetWithHeader(String path, String name, String value) {
        HttpHeaders h = new HttpHeaders();
        h.add(name, value);
        lastResponse = rest.exchange(url(path), HttpMethod.GET, new HttpEntity<>(h), String.class);
    }

    @When("I GET the booking form for that flight")
    public void iGetBookingForm() {
        lastResponse = rest.getForEntity(url("/booking/new?flightId=" + flightIdUnderTest), String.class);
    }

    @When("I POST a booking form with")
    public void iPostBookingForm(DataTable table) {
        Map<String, String> fields = table.asMap(String.class, String.class);
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("flightId", String.valueOf(flightIdUnderTest));
        fields.forEach(body::add);
        lastResponse = rest.postForEntity(url("/booking"), new HttpEntity<>(body, h), String.class);
    }

    @Then("the response status is {int}")
    public void responseStatus(int expected) {
        assertEquals(expected, lastResponse.getStatusCodeValue());
    }

    @Then("the response status is one of {int}, {int}")
    public void responseStatusOneOf(int a, int b) {
        int got = lastResponse.getStatusCodeValue();
        assertTrue(got == a || got == b, "status=" + got + " not in {" + a + "," + b + "}");
    }

    @Then("the response content-type contains {string}")
    public void contentTypeContains(String needle) {
        String ct = lastResponse.getHeaders().getContentType() == null
                ? "" : lastResponse.getHeaders().getContentType().toString();
        assertTrue(ct.contains(needle), "content-type=" + ct);
    }

    @Then("the body contains {string}")
    public void bodyContains(String needle) {
        String body = lastResponse.getBody();
        assertNotNull(body, "empty body");
        assertTrue(body.contains(needle),
                "body did not contain '" + needle + "' — actual: " + body.substring(0, Math.min(body.length(), 500)));
    }

    @Then("the location header ends with {string}")
    public void locationEndsWith(String suffix) {
        String loc = lastResponse.getHeaders().getFirst("Location");
        assertNotNull(loc, "missing Location header");
        assertTrue(loc.endsWith(suffix), "Location=" + loc);
    }

    @Then("the location header contains {string}")
    public void locationContains(String needle) {
        String loc = lastResponse.getHeaders().getFirst("Location");
        assertNotNull(loc, "missing Location header");
        assertTrue(loc.contains(needle), "Location=" + loc);
    }
}
