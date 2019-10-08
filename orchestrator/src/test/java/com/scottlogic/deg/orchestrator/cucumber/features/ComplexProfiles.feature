Feature: Generator can produce correct data for complex profiles.

  Scenario: Running a random strategy on a profile where there are hard to detect contradictions within or statements does not crash
    Given the generation strategy is Random
    And the generator can generate at most 5 rows
    And the following fields exist:
      | foo |
      | bar |
    And foo has type "integer"
    And foo is anything but null
    And bar has type "integer"
    And bar is anything but null
    And there is a constraint:
      """
        {
          "anyOf": [
            {
              "allOf": [
                {
                  "anyOf": [
                  {
                    "equalTo":
                      {
                        "field": "bar", "value": 1
                      }
                  },
                  {
                      "equalTo":
                      {
                          "field": "bar", "value": 2
                      }
                  }
                  ]
                },
                {
                  "anyOf": [
                    { "equalTo": { "field": "foo", "value": 1 } },
                     { "equalTo": { "field": "bar", "value": 3 } }
                  ]
                },
                {
                  "anyOf": [
                     { "equalTo": { "field": "foo", "value": 2 } },
                     { "equalTo": { "field": "bar", "value": 4 } }
                  ]
                }
              ]
            },
            {
              "allOf": [
                 { "equalTo": { "field": "foo", "value": 10 } },
                 { "equalTo": { "field": "bar", "value": 10 } }
              ]
            }
          ]
        }
      """
    Then the following data should be generated:
      | foo | bar |
      | 10  | 10  |
      | 10  | 10  |
      | 10  | 10  |
      | 10  | 10  |
      | 10  | 10  |
