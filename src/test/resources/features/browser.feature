@browser
Feature: Real-browser user journey
  As a customer using a web browser
  I want to browse and book on denkair.de
  So that the pages render and the happy-path form works end-to-end.

  Scenario: Home page renders in Chrome
    Given I open "/" in a real browser
    Then the page title contains "DenkAir"
    And the page body contains "Palma"

  Scenario: Clicking a destination tile lands on the detail page
    Given I open "/ziele" in a real browser
    When I click the first link whose href contains "/ziele/palma-mallorca"
    Then the page URL contains "/ziele/palma-mallorca"
    And the page body contains "Palma"

  Scenario: Completing a booking via the form
    Given I open "/booking/new?flightId=1" in a real browser
    When I fill in "firstName" with "Max"
    And I fill in "lastName" with "Browser"
    And I fill in "email" with "browser@example.de"
    And I fill in "passengers" with "2"
    And I submit the booking form
    Then the page URL contains "/booking/HA-"
    And the page body contains "Buchung"
