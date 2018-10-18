Feature: User can specify that a value either matches a regex in the form of an ISIN code

  Background:
    Given the generation strategy is interesting

  Scenario: User using matchingRegex operator to provide an exact set of values which represent an ISIN
    Given there is a field foo
    And foo is a valid "ISIN"
    And foo is anything but null
    Then the following data should be included in what is generated:
      | foo   |
      | "XS0255015603"  |
      | "XS0255015603"  |
      | "US0378331005"  |
