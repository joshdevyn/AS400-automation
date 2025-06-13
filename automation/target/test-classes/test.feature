Feature: AS400 General Test
  As a QA automation engineer
  I want to run general tests on the AS400 system
  So that I can validate connectivity and basic flows

  Background:
    Given the AS400 system is available
    And the terminal emulator is initialized with CTT file "test-sample.ctt"


  @ctt @as400 @config:test-sample.ctt
  Scenario Outline: 1. Login
    Given I am connected to NRO
    And I login with "<user>" and "<password>"
    And I should be on the main page of "<environment>"
    And I navigate to contract creation
    And I add personal client
    Then I create a new person
    And I add personal client
    Then I create a new person
    And I add personal client
    Then I create a new person
    And I add personal client
    Then I create a new person
    And I add personal client
    Then I create a new person


    Examples:
      | user    | password   | environment |
      | GIUROAL | Bucuresti2 | 72          |
