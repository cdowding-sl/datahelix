Feature: User can specify that one date should be equal to another date

  Background:
    Given the generation strategy is full
    And there is a field foo
    And foo has type "datetime"
    And there is a field bar
    And bar has type "datetime"

  Scenario: Running an "equalToField" constraint allows one date to be always equal to another
    Given foo is equal to 2018-09-01T00:00:00.000Z
    And the generator can generate at most 1 rows
    And there is a constraint:
      """
        {
          "field": "foo",
          "is": "equalTo",
          "otherField": "bar"
        }
      """
    Then the following data should be generated:
      | foo                      | bar                      |
      | 2018-09-01T00:00:00.000Z | 2018-09-01T00:00:00.000Z |

  @ignore #other field functionality is broken
  Scenario: Running an "equalToField" constraint allows one date to be always equal to another with a positive offset
    Given the generator can generate at most 1 rows
    And there is a constraint:
      """
        {
          "field": "bar",
          "is": "equalTo",
          "otherField": "foo",
          "offset": 3,
          "offsetUnit": "days"
        }
      """
    Then the following data should be generated:
      | foo                      | bar                      |
      | 0001-01-01T00:00:00.000Z | 0001-01-04T00:00:00.000Z |

  @ignore #other field functionality is broken
  Scenario: Running an "equalToField" constraint allows one date to be always equal to another with a negative offset
    Given foo is after 2018-01-04T00:00:00.000Z
    And the generator can generate at most 1 rows
    And there is a constraint:
      """
        {
          "field": "foo",
          "is": "equalTo",
          "otherField": "bar",
          "offset": -3,
          "offsetUnit": "days"
        }
      """
    Then the following data should be generated:
      | foo                      | bar                      |
      | 2018-01-04T00:00:00.001Z | 2018-01-07T00:00:00.001Z |

    # Results accomodate for the fact that the 5 working days include non-working days
  @ignore #other field functionality is broken
  Scenario: Running an "equalToField" constraint allows one date to be always equal to another plus a value in working days
    Given the generator can generate at most 1 rows
    And there is a constraint:
      """
        {
          "field": "bar",
          "is": "equalTo",
          "otherField": "foo",
          "offset": 5,
          "offsetUnit": "working days"
        }
      """
    Then the following data should be generated:
      | foo                      | bar                      |
      | 0001-01-01T00:00:00.000Z | 0001-01-08T00:00:00.000Z |

    # Results accomodate for the fact that the 5 working days include non-working days
  @ignore #other field functionality is broken
  Scenario: Running an "equalToField" constraint allows one date to be always equal to another minus a value in working days
    Given the generator can generate at most 1 rows
    And there is a constraint:
      """
        {
          "field": "foo",
          "is": "equalTo",
          "otherField": "bar",
          "offset": -5,
          "offsetUnit": "working days"
        }
      """
    Then the following data should be generated:
      | foo                      | bar                      |
      | 0001-01-01T00:00:00.000Z | 0001-01-08T00:00:00.000Z |

