Feature: User can specify that contains a specified regex

  Background:
    Given the generation strategy is full
    And there is a field foo
    And foo is of type "string"

  Scenario: Running a 'containingRegex' request that includes roman alphabet lowercase chars (a-z) only should be successful
    Given foo is containing regex /[a-z]{1}/
    And foo is of length 1
    Then the following data should be generated:
      | foo  |
      | null |
      | "a"  |
      | "b"  |
      | "c"  |
      | "d"  |
      | "e"  |
      | "f"  |
      | "g"  |
      | "h"  |
      | "i"  |
      | "j"  |
      | "k"  |
      | "l"  |
      | "m"  |
      | "n"  |
      | "o"  |
      | "p"  |
      | "q"  |
      | "r"  |
      | "s"  |
      | "t"  |
      | "u"  |
      | "v"  |
      | "w"  |
      | "x"  |
      | "y"  |
      | "z"  |

  Scenario: Running a 'containingRegex' request that includes roman alphabet uppercase chars (A-Z) only should be successful
    Given foo is containing regex /[A-Z]{1}/
    And foo is of length 1
    Then the following data should be generated:
      | foo  |
      | null |
      | "A"  |
      | "B"  |
      | "C"  |
      | "D"  |
      | "E"  |
      | "F"  |
      | "G"  |
      | "H"  |
      | "I"  |
      | "J"  |
      | "K"  |
      | "L"  |
      | "M"  |
      | "N"  |
      | "O"  |
      | "P"  |
      | "Q"  |
      | "R"  |
      | "S"  |
      | "T"  |
      | "U"  |
      | "V"  |
      | "W"  |
      | "X"  |
      | "Y"  |
      | "Z"  |

  Scenario: Running a 'containingRegex' request that includes roman numeric chars (0-9) only should be successful
    Given foo is containing regex /[0-9]{1}/
    And foo is of length 1
    Then the following data should be generated:
      | foo  |
      | null |
      | "0"  |
      | "1"  |
      | "2"  |
      | "3"  |
      | "4"  |
      | "5"  |
      | "6"  |
      | "7"  |
      | "8"  |
      | "9"  |

  Scenario: Running a 'containingRegex' request that includes basic punctuation characters (!-.) only should be successful
    Given foo is containing regex /[!-.]{1}/
    And foo is of length 1
    Then the following data should be generated:
      | foo  |
      | null |
      | "!"  |
      | """  |
      | "#"  |
      | "$"  |
      | "%"  |
      | "&"  |
      | "'"  |
      | "("  |
      | ")"  |
      | "*"  |
      | "+"  |
      | ","  |
      | "-"  |
      | "."  |

  Scenario: Running a 'containingRegex' request that includes anchors ^ and $ should be successful
    Given foo is containing regex /^[a-c]{1}$/
    And foo is of length 1
    Then the following data should be generated:
      | foo  |
      | null |
      | "a"  |
      | "b"  |
      | "c"  |

  Scenario: containingRegex that does not include the closing anchor '$' should be successful
    Given foo is containing regex /^[a-c]{1}/
    And foo is of length 1
    Then the following data should be generated:
      | foo  |
      | null |
      | "a"  |
      | "b"  |
      | "c"  |

  Scenario: containingRegex that does not include the opening anchor '^' should be successful
    Given foo is containing regex /[a-c]{1}$/
    And foo is of length 1
    Then the following data should be generated:
      | foo  |
      | null |
      | "a"  |
      | "b"  |
      | "c"  |

  Scenario: Running a 'containingRegex' request for a single character (a) should be successful
    Given foo is containing regex /[a]{1}/
    And foo is of length 1
    Then the following data should be generated:
      | foo  |
      | null |
      | "a"  |

  Scenario: Running a 'containingRegex' request for a range over a single character ([a-a]) should be successful
    Given foo is containing regex /[a-a]{1}/
    And foo is of length 1
    Then the following data should be generated:
      | foo  |
      | null |
      | "a"  |

  Scenario: Running a 'containingRegex' for a maximum length smaller than the minimum length should fail with an error
    Given foo is containing regex /[a]{1,0}/
    Then the profile is invalid because "Field \[foo\]: Illegal repetition range near index 7\r?\n\[a\]\{1,0\}\r?\n       \^"
    And no data is created

  Scenario: Running a 'containingRegex' for a minimum length of a decimal value should fail with an error
    Given foo is containing regex /[a]{1.1}/
    Then the profile is invalid because "Field \[foo\]: Unclosed counted closure near index 5\r?\n\[a\]\{1.1\}\r?\n     \^"
    And no data is created

  Scenario: Running a 'containingRegex' for a minimum length that is less zero should fail with an error message
    Given foo is containing regex /[a]{-1}/
    Then the profile is invalid because "Field \[foo\]: Illegal repetition near index 2\r?\n\[a\]\{-1\}\r?\n  \^"
    And no data is created

  Scenario: Running a 'containingRegex' for an empty value should fail with an error message
    Given foo is containing regex /[]{}/
    Then the profile is invalid because "Field \[foo\]: Unclosed character class near index 3\r?\n\[\]\{\}\r?\n   \^"
    And no data is created

  Scenario: Running a 'containingRegex' request with the value property set to a null entry (null) should throw an error
    Given foo is containing regex null
    Then the profile is invalid because "Field \[foo\]: Couldn't recognise 'value' property, it must be set to a value"
    And no data is created

  Scenario: containingRegex run against a non contradicting containingRegex should be successful
    Given foo is containing regex /[b]{2}/
    And foo is containing regex /[a-z]{1,3}/
    And foo is of length 2
    Then the following data should be generated:
      | foo  |
      | null |
      | "bb" |

  Scenario: containingRegex run against a non contradicting not containingRegex should be successful
    Given foo is containing regex /[b]{2}/
    And foo is anything but containing regex /[a]{2}/
    And foo is of length 2
    Then the following data should be generated:
      | foo  |
      | null |
      | "bb" |

  Scenario: containingRegex run against a contradicting not containingRegex should only generate null
    Given foo is containing regex /[b]{1}/
    And foo is anything but containing regex /[b]{1}/
    And foo is of length 1
    Then the following data should be generated:
      | foo  |
      | null |

  Scenario: containingRegex run against a non contradicting ofLength should be successful
    Given foo is containing regex /[b]{2}/
    And foo is of length 2
    Then the following data should be generated:
      | foo  |
      | null |
      | "bb" |

  Scenario: containingRegex run against a non contradicting not ofLength should be successful
    Given foo is containing regex /[b]{2}/
    And foo is anything but of length 1
    And foo is in set:
      | "b"   |
      | "bb"  |
      | "bbb" |
    Then the following data should be generated:
      | foo   |
      | null  |
      | "bb"  |
      | "bbb" |

  Scenario: not containingRegex run against a non contradicting ofLength should be successful
    Given foo is anything but containing regex /[b]{1}/
    And foo is matching regex /[a-e]{1}/
    And foo is of length 1
    Then the following data should be generated:
      | foo  |
      | null |
      | "a"  |
      | "c"  |
      | "d"  |
      | "e"  |

  Scenario: containingRegex run against a contradicting not ofLength should be successful
    Given foo is containing regex /[b]{1}/
    And foo is matching regex /[a-d]{2}/
    And foo is anything but of length 1
    Then the following data should be generated:
      | foo  |
      | null |
      | "ab" |
      | "ba" |
      | "bb" |
      | "bc" |
      | "bd" |
      | "cb" |
      | "db" |

  Scenario: containingRegex run against a contradicting ofLength should only generate null
    Given foo is containing regex /[b]{2}/
    And foo is of length 1
    And foo is in set:
      | "a"   |
      | "bb"  |
      | "abb" |
    Then the following data should be generated:
      | foo  |
      | null |

  Scenario: containingRegex run against a non contradicting longerThan should be successful
    Given foo is containing regex /[b]{2}/
    And foo is longer than 2
    And foo is in set:
      | "b"   |
      | "bb"  |
      | "bbb" |
    Then the following data should be generated:
      | foo   |
      | null  |
      | "bbb" |

  Scenario: containingRegex run against a non contradicting not longerThan should be successful
    Given foo is containing regex /[b]{2}/
    And foo is anything but longer than 2
    And foo is in set:
      | "b"   |
      | "bb"  |
      | "bbb" |
    Then the following data should be generated:
      | foo  |
      | null |
      | "bb" |

  Scenario: not containingRegex run against a non contradicting longerThan should be successful
    Given foo is anything but containing regex /[b]{2}/
    And foo is longer than 2
    And foo is in set:
      | "a"   |
      | "aa"  |
      | "aaa" |
      | "b"   |
      | "bb"  |
      | "bbb" |
    Then the following data should be generated:
      | foo   |
      | null  |
      | "aaa" |

  Scenario: containingRegex run against a contradicting not longerThan should only generate null
    Given foo is containing regex /[b]{2}/
    And foo is anything but longer than 1
    Then the following data should be generated:
      | foo  |
      | null |

  Scenario: containingRegex run against a non contradicting shorterThan should be successful
    Given foo is containing regex /[b]{2}/
    And foo is shorter than 3
    And foo is in set:
      | "b"   |
      | "bb"  |
      | "bbb" |
    Then the following data should be generated:
      | foo  |
      | null |
      | "bb" |

  Scenario: containingRegex run against a non contradicting not shorterThan should be successful
    Given foo is containing regex /[b]{2}/
    And foo is anything but shorter than 2
    And foo is in set:
      | "b"   |
      | "bb"  |
      | "bbb" |
    Then the following data should be generated:
      | foo   |
      | null  |
      | "bb"  |
      | "bbb" |

  Scenario: not containingRegex run against a non contradicting shorterThan should be successful
    Given foo is anything but containing regex /[b]{2}/
    And foo is shorter than 3
    And foo is in set:
      | "a"   |
      | "aa"  |
      | "aaa" |
      | "b"   |
      | "bb"  |
      | "bbb" |
    Then the following data should be generated:
      | foo  |
      | null |
      | "a"  |
      | "aa" |
      | "b"  |

  Scenario: containingRegex run against a contradicting shorterThan should only generate null
    Given foo is containing regex /[b]{3}/
    And foo is shorter than 2
    Then the following data should be generated:
      | foo  |
      | null |
