Feature: Public browsing
  As an anonymous visitor to denkair.de
  I want to see the home page and search for flights
  So that I can pick a destination before booking.

  Scenario: Home page renders
    When I GET "/"
    Then the response status is 200
    And the response content-type contains "text/html"
    And the body contains "DenkAir"

  Scenario: Swagger UI is exposed
    When I GET "/swagger-ui.html"
    Then the response status is 200

  Scenario: Actuator health is reachable
    # May return 503 if the Mail healthcheck can't reach smtp.denkair.de from the test host.
    # Either way Actuator is up and speaks JSON.
    When I GET "/actuator/health"
    Then the response status is one of 200, 503
    And the body contains "status"

  Scenario: Search HAM to PMI returns JSON list
    When I GET "/flights/api/search?origin=HAM&destination=PMI"
    Then the response status is 200
    And the response content-type contains "json"
    # H2 in MySQL-mode returns uppercased column names from raw JdbcTemplate.
    And the body contains "FLIGHT_NUMBER"

  Scenario: Destination page for Palma de Mallorca
    When I GET "/ziele/palma-mallorca"
    Then the response status is 200
    And the body contains "Palma"

  Scenario: Unknown destination slug redirects to overview
    When I GET without following redirects "/ziele/atlantis"
    Then the response status is 302
    And the location header ends with "/ziele"
