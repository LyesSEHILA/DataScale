Feature: Complete Quiz Journey

  Scenario: User completes a quiz session
    Given the server is running
    When I create a session with age 25
    Then I receive status 201 and a session ID
    And I can fetch questions for this session
    And I can submit an answer to the first question
    And I can fetch my final results