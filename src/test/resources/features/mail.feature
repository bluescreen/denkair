Feature: Booking confirmation email
  As a newly-booked customer
  I want to receive a confirmation email from DenkAir
  So that I have proof of booking and the flight details.

  Background:
    Given I pick a seeded active flight

  Scenario: Confirmation email is sent for a successful booking
    When I POST a booking form with
      | firstName  | Max             |
      | lastName   | Mailtest        |
      | email      | mail@example.de |
      | passengers | 2               |
    Then the response status is 302
    And the inbox for "mail@example.de" has 1 message
    And the latest mail to "mail@example.de" has subject containing "Buchungsbestätigung"
    And the latest mail to "mail@example.de" body contains "HA-"
    And the latest mail to "mail@example.de" body contains "Passagiere: 2"
    And the latest mail to "mail@example.de" is from "no-reply@denkair.de"

  Scenario: Confirmation includes flight number, route template, and price
    # Don't hard-code origin/destination — pickSeededFlight() grabs the
    # earliest-departure flight, which varies with seeding.
    When I POST a booking form with
      | firstName  | Route            |
      | lastName   | Check            |
      | email      | route@example.de |
      | passengers | 1                |
    Then the response status is 302
    And the latest mail to "route@example.de" body contains "Flug: HA4"
    And the latest mail to "route@example.de" body contains "Von:"
    And the latest mail to "route@example.de" body contains "Nach:"
    And the latest mail to "route@example.de" body contains "Gesamtpreis:"
    And the latest mail to "route@example.de" body contains "Vielen Dank"
