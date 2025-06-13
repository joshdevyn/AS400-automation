Feature: New Contract Creation and Management
  As a business user
  I want to create and manage new contracts
  So that I can process customer agreements efficiently

  Background:
    Given I am connected to NRO "72" with "GIUROAL" and "Bucuresti2"
    And I navigate to New Contract Proposal

  @smoke @contract
  Scenario Outline: Create new contract proposal
    Given I create a new Contract of type "<type>"
    When I set the contract owner to "<owner>"
    And I set the date to "<date>"
    And I set the billing frequency to "<freq>"
    And I set the method of payment to "<method>"
    And I set the serial number to "<serial>"
    And I set the agent to "<agent>"
    Then I submit the contract
    And the contract should be created successfully
    Then I go back to the main menu

    Examples:
      | type  | owner     | date       | freq | method | serial | agent  |
      | 1R1   | Adrian    | 01/01/2025 | M    | C      | 1337   | 75540  |
      | 2R1   | Maria     | 15/02/2025 | Q    | D      | 1338   | 75541  |
      | 1R2   | John      | 01/03/2025 | A    | C      | 1339   | 75542  |
