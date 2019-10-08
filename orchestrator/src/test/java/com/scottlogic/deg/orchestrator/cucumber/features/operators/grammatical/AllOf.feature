Feature: User can specify that data must be created to conform to each of multiple specified constraints.

  Background:
    Given the generation strategy is full

  Scenario: Running an 'allOf' request that contains a valid nested allOf request should be successful
    Given there is a field foo
    And foo has type "string"
    And there is a constraint:
      """
      { "allOf": [
        { "allOf": [
          { "matchingRegex": { "field": "foo", "value": "[a-b]{2}"} },
          { "ofLength": {"field": "foo", "value": 2 } }
        ]},
        { "shorterThan": {"field": "foo", "value": 3 } }
      ]}
      """
    Then the following data should be generated:
      | foo  |
      | "aa" |
      | "ab" |
      | "bb" |
      | "ba" |
      | null |

  Scenario: Running an 'allOf' request that contains an invalid nested allOf request should generate null
    Given there is a field foo
    And foo has type "string"
    And there is a constraint:
      """
      { "allOf": [
        { "allOf": [
          { "matchingRegex":  {"field": "foo", "value": "[a-k]{3}" }},
          { "matchingRegex":  {"field": "foo",  "value": "[1-5]{3}" } }
        ]},
        { "longerThan": { "field": "foo",  "value": 4 }}
      ]}
      """
    Then the following data should be generated:
      | foo  |
      | null |

  Scenario: Running a 'allOf' request that includes multiple values within the same statement should be successful
    Given there is a field foo
    And foo has type "string"
    And there is a constraint:
      """
      { "allOf": [
        { "equalTo": {  "field": "foo",  "value": "Test01" }},
         { "equalTo": { "field": "foo", "value": "Test01" }}
      ]}
      """
    Then the following data should be generated:
      | foo      |
      | "Test01" |

  Scenario: User attempts to combine two constraints that only intersect at the empty set within an allOf operator should not generate data
    Given there is a field foo
    And foo has type "string"
    And there is a constraint:
      """
      { "allOf": [
        { "equalTo": { "field": "foo", "value": "Test0" }},
         { "equalTo": { "field": "foo", "value": "5"} }
      ]}
      """
    Then no data is created
