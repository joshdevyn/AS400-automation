Feature: AS400 Terminal Connection and Authentication
  As a QA automation engineer
  I want to establish secure connections to AS400 systems
  So that I can automate business process testing

  Background:
    Given the AS400 system is available
    And the terminal emulator is initialized with CTT file "test-sample.ctt"

  @ctt @as400 @config:test-sample.ctt @smoke @authentication
  Scenario: Successful login with valid credentials
    Given I have valid credentials for NRO "72"
    When I connect with username "GIUROAL" and password "Bucuresti2"
    Then I should be successfully logged into the system
    And I should see the main menu

