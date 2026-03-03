# Implementation Plan

- [x] 1. Write bug condition exploration test
  - **Property 1: Fault Condition** - Map Picker Opens Without Crash
  - **CRITICAL**: This test MUST FAIL on unfixed code - failure confirms the bug exists
  - **DO NOT attempt to fix the test or the code when it fails**
  - **NOTE**: This test encodes the expected behavior - it will validate the fix when it passes after implementation
  - **GOAL**: Surface counterexamples that demonstrate the IncompatibleClassChangeError crash
  - **Scoped PBT Approach**: Scope the property to the concrete failing case - opening MapPicker with javafx-web 17.0.14 and javafx-controls/fxml 20.0.2
  - Test that opening MapPicker dialog triggers IncompatibleClassChangeError when javafx-web version is 17.0.14 while other JavaFX modules are at 20.0.2
  - The test should simulate clicking "Open Map" button or directly instantiating MapPicker and initializing WebView
  - Test assertions should verify: dialog opens successfully, WebView initializes, map tiles load without throwing IncompatibleClassChangeError
  - Run test on UNFIXED code (javafx-web 17.0.14)
  - **EXPECTED OUTCOME**: Test FAILS with IncompatibleClassChangeError (this is correct - it proves the bug exists)
  - Document counterexamples found: stack trace showing WCImageDecoderImpl.getImageFrames and ImageStorage.loadAll() incompatibility
  - Mark task complete when test is written, run, and failure is documented
  - _Requirements: 2.1, 2.2, 2.3_

- [ ] 2. Write preservation property tests (BEFORE implementing fix)
  - **Property 2: Preservation** - Existing JavaFX Components Function Correctly
  - **IMPORTANT**: Follow observation-first methodology
  - Observe behavior on UNFIXED code for non-MapPicker JavaFX components
  - Test Case 1: Verify all FXML views (payment forms, marketplace views, event management views) load correctly
  - Test Case 2: Verify JavaFX controls (buttons, text fields, tables) respond correctly to user interactions
  - Test Case 3: Verify other dialogs (non-MapPicker) open and function correctly
  - Test Case 4: Verify application startup and navigation between views works correctly
  - Write property-based tests capturing observed behavior patterns: for all non-MapPicker interactions, application behaves identically
  - Property-based testing generates many test cases for stronger guarantees
  - Run tests on UNFIXED code (javafx-web 17.0.14, javafx-controls/fxml 20.0.2)
  - **EXPECTED OUTCOME**: Tests PASS (this confirms baseline behavior to preserve)
  - Mark task complete when tests are written, run, and passing on unfixed code
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [ ] 3. Fix JavaFX version mismatch in pom.xml

  - [ ] 3.1 Update javafx-web dependency version
    - Locate the `<dependency>` block for `org.openjfx:javafx-web` in firma/pom.xml
    - Update the `<version>` tag from `17.0.14` to `20.0.2`
    - Verify javafx-controls is at version 20.0.2
    - Verify javafx-fxml is at version 20.0.2
    - Ensure all three JavaFX modules are now aligned at version 20.0.2
    - Run `mvn clean install` to download the correct version and rebuild
    - _Bug_Condition: isBugCondition(input) where input.javafxWebVersion = "17.0.14" AND input.javafxControlsVersion = "20.0.2" AND input.javafxFxmlVersion = "20.0.2" AND input.attemptingToLoadMapTiles = true_
    - _Expected_Behavior: MapPicker dialog opens successfully, WebView initializes, map tiles load without IncompatibleClassChangeError_
    - _Preservation: All existing FXML views, controls, dialogs, and JavaFX components continue to function identically with version 20.0.2_
    - _Requirements: 2.1, 2.2, 2.3, 3.1, 3.2, 3.3, 3.4, 3.5_

  - [ ] 3.2 Verify bug condition exploration test now passes
    - **Property 1: Expected Behavior** - Map Picker Opens Without Crash
    - **IMPORTANT**: Re-run the SAME test from task 1 - do NOT write a new test
    - The test from task 1 encodes the expected behavior
    - When this test passes, it confirms the expected behavior is satisfied
    - Run bug condition exploration test from step 1
    - **EXPECTED OUTCOME**: Test PASSES (confirms bug is fixed - MapPicker opens without crash)
    - _Requirements: 2.1, 2.2, 2.3_

  - [ ] 3.3 Verify preservation tests still pass
    - **Property 2: Preservation** - Existing JavaFX Components Function Correctly
    - **IMPORTANT**: Re-run the SAME tests from task 2 - do NOT write new tests
    - Run preservation property tests from step 2
    - **EXPECTED OUTCOME**: Tests PASS (confirms no regressions in other JavaFX components)
    - Confirm all tests still pass after fix (no regressions)
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [ ] 4. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
