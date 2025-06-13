Feature: AS400 Business Navigation
  As a QA automation engineer
  I want to automate navigation through AS400 business menus
  So that I can validate business process flows
  Background:
    Given the AS400 system is available
    And the terminal emulator is initialized with CTT file "test-sample.ctt"
    And I am connected to NRO "72" with "GIUROAL" and "Bucuresti2"

