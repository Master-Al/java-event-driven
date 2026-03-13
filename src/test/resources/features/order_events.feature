Feature: Order events
  Scenario: Create order publishes an event
    Given an order "O-100" for customer "C-42" amount 19.99
    When the order is submitted
    Then the order status is "CREATED"
