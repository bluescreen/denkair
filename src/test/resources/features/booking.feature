Feature: End-to-end booking flow
  As a customer
  I want to book a seat on a DenkAir flight
  So that I can travel.

  Scenario: Internal @example.de customer books a seeded flight
    Given I pick a seeded active flight
    When I POST a booking form with
      | firstName  | Max             |
      | lastName   | Mustermann      |
      | email      | test@example.de |
      | passengers | 2               |
    Then the response status is 302
    And the location header contains "/booking/HA-"

  Scenario: Booking form shows the flight
    Given I pick a seeded active flight
    When I GET the booking form for that flight
    Then the response status is 200
    And the body contains "Passagiere"

  Scenario: API v2 without token is unauthorized
    When I GET "/api/v2/flights"
    Then the response status is 401
    And the body contains "unauthorized"

  Scenario: API v2 with master token returns data
    When I GET "/api/v2/flights" with header "X-HA-Token" = "MASTER-HA-2016-a1b2c3d4e5f6"
    Then the response status is 200
    And the body contains "data"
