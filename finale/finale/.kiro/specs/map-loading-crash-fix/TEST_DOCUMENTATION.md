# Bug Condition Exploration Test Documentation

## Test Location
`firma/src/test/java/Firma/tools/GestionMarketplace/MapPickerBugConditionTest.java`

## Purpose
This test is designed to **surface counterexamples** that demonstrate the IncompatibleClassChangeError crash on unfixed code. It validates that the root cause hypothesis (JavaFX version mismatch) is correct.

## Test Properties

### Property 1: MapPicker Opens Without IncompatibleClassChangeError
**Validates: Requirements 2.1, 2.2, 2.3**

This property-based test uses jqwik to generate random address and city strings and attempts to open the MapPicker dialog with each combination. The test:

1. Initializes JavaFX toolkit
2. Creates a Stage as the dialog owner
3. Instantiates MapPicker
4. Calls `showAndWait()` which triggers WebView initialization
5. Waits for WebView to load map tiles (this is where the crash occurs)
6. Automatically closes the dialog after 2 seconds

**Expected Outcome on UNFIXED Code (javafx-web 17.0.14):**
- Test **FAILS** with `IncompatibleClassChangeError`
- Error message: "Expected static method 'com.sun.javafx.iio.ImageFrame[] com.sun.javafx.iio.ImageStorage.loadAll(...)'"
- Stack trace shows: `WCImageDecoderImpl.getImageFrames` → `ImageStorage.loadAll()`
- This **confirms the bug exists** and the root cause is correct

**Expected Outcome on FIXED Code (javafx-web 20.0.2):**
- Test **PASSES** - MapPicker opens successfully
- WebView initializes without errors
- Map tiles load and display correctly
- This **confirms the fix works**

### Property 2: WebView Initializes Without IncompatibleClassChangeError
**Validates: Requirements 2.2, 2.3**

This is a simplified, more focused test that directly tests WebView initialization with image decoding:

1. Creates a WebView instance
2. Loads HTML content with a base64-encoded PNG image
3. Waits for the image to be decoded

**Expected Outcome on UNFIXED Code:**
- Test **FAILS** with `IncompatibleClassChangeError` during image decoding
- This isolates the exact failure point to WebView's image decoder

**Expected Outcome on FIXED Code:**
- Test **PASSES** - WebView successfully decodes the image

## How to Run

### Prerequisites
- Maven must be installed and in PATH
- Java 17 or higher
- JavaFX dependencies in pom.xml (already configured)

### Run All Tests
```bash
cd firma
mvn clean test -Dtest=MapPickerBugConditionTest
```

### Run with Specific Number of Tries
```bash
mvn clean test -Dtest=MapPickerBugConditionTest -Djqwik.tries=20
```

### Run Specific Property
```bash
# Run only the MapPicker property
mvn clean test -Dtest=MapPickerBugConditionTest#mapPickerOpensWithoutCrash

# Run only the WebView property
mvn clean test -Dtest=MapPickerBugConditionTest#webViewInitializesWithoutCrash
```

## Current Status

**Maven is not installed on this system.** The test has been written and is ready to run, but requires Maven to execute.

### To Install Maven:
1. Download from: https://maven.apache.org/download.cgi
2. Extract to a directory (e.g., `C:\Program Files\Apache\maven`)
3. Add `bin` directory to PATH environment variable
4. Verify: `mvn -version`

### Alternative: Use IDE
If you're using IntelliJ IDEA or Eclipse:
1. Open the project in your IDE
2. Right-click on `MapPickerBugConditionTest.java`
3. Select "Run 'MapPickerBugConditionTest'"
4. The IDE will handle Maven dependencies automatically

## Expected Counterexamples

When this test runs on unfixed code, it will produce counterexamples like:

```
java.lang.IncompatibleClassChangeError: Expected static method 
'com.sun.javafx.iio.ImageFrame[] com.sun.javafx.iio.ImageStorage.loadAll(
    java.io.InputStream, 
    com.sun.javafx.iio.ImageLoadListener, 
    double, double, boolean, float, boolean)'

at com.sun.webkit.graphics.WCImageDecoderImpl.getImageFrames(WCImageDecoderImpl.java:XX)
at com.sun.webkit.graphics.WCImageDecoderImpl.loadFrames(WCImageDecoderImpl.java:XX)
at com.sun.webkit.graphics.WCImageImpl.getFrames(WCImageImpl.java:XX)
...
```

These counterexamples prove:
1. The bug exists in the current codebase
2. The root cause is the version mismatch between javafx-web 17.0.14 and javafx-controls/fxml 20.0.2
3. The crash occurs specifically when WebView tries to decode images (map tiles)

## Test Configuration

The test uses jqwik property-based testing with:
- **tries = 10**: Each property runs 10 times with different random inputs
- **String generators**: Generate random addresses (0-100 chars) and cities (0-50 chars)
- **Timeout**: 10 seconds per test to prevent hanging
- **Headless mode**: Uses JFXPanel to initialize JavaFX without a display

## Next Steps

1. **Install Maven** (if not already installed)
2. **Run the test** on unfixed code to confirm it fails
3. **Document the counterexamples** (stack traces, error messages)
4. **Implement the fix** (upgrade javafx-web to 20.0.2)
5. **Re-run the test** to confirm it passes after the fix
