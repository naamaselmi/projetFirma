# How to Run Bug Condition Exploration Test in IntelliJ IDEA

## Step-by-Step Instructions

### 1. Open Project in IntelliJ IDEA
- If not already open, open IntelliJ IDEA
- Open the `firma` project folder
- Wait for IntelliJ to index the project (bottom right corner shows progress)

### 2. Sync Maven Dependencies
IntelliJ should automatically detect the pom.xml changes and download dependencies.

If not automatic:
- Right-click on `firma/pom.xml`
- Select "Maven" → "Reload project"
- Wait for dependencies to download (jqwik will be downloaded)
- Check bottom right corner for download progress

### 3. Navigate to Test File
**Option A: Simple JUnit Test (Recommended for First Run)**
- Navigate to: `firma/src/test/java/Firma/tools/GestionMarketplace/SimpleWebViewTest.java`
- This is easier to understand and faster to run

**Option B: Property-Based Test**
- Navigate to: `firma/src/test/java/Firma/tools/GestionMarketplace/MapPickerBugConditionTest.java`
- This runs multiple test cases with random inputs

### 4. Run the Test

#### For SimpleWebViewTest:
1. Open `SimpleWebViewTest.java` in the editor
2. You'll see green "play" icons next to the class name and each test method
3. Click the green play icon next to the class name
4. Select "Run 'SimpleWebViewTest'"
5. Wait for test execution (should take 5-10 seconds)

#### For MapPickerBugConditionTest:
1. Open `MapPickerBugConditionTest.java` in the editor
2. Click the green play icon next to the class name
3. Select "Run 'MapPickerBugConditionTest'"
4. Wait for test execution (may take 30-60 seconds due to multiple tries)

### 5. Observe Test Results

The test will appear in the "Run" panel at the bottom of IntelliJ.

#### Expected Result: TEST FAILS ❌
This is **CORRECT** and **EXPECTED** on unfixed code!

**You should see:**
```
java.lang.IncompatibleClassChangeError: Expected static method 
'com.sun.javafx.iio.ImageFrame[] com.sun.javafx.iio.ImageStorage.loadAll(
    java.io.InputStream, 
    com.sun.javafx.iio.ImageLoadListener, 
    double, double, boolean, float, boolean)'

at com.sun.webkit.graphics.WCImageDecoderImpl.getImageFrames(WCImageDecoderImpl.java:XX)
at com.sun.webkit.graphics.WCImageDecoderImpl.loadFrames(WCImageDecoderImpl.java:XX)
...
```

**Console Output:**
```
=== EXPECTED ERROR ON UNFIXED CODE ===
Error: java.lang.IncompatibleClassChangeError
Message: Expected static method 'com.sun.javafx.iio.ImageFrame[] com.sun.javafx.iio.ImageStorage.loadAll(...)'
This confirms javafx-web 17.0.14 is incompatible with javafx-controls/fxml 20.0.2
=====================================
```

### 6. Document the Counterexample

Copy the full error message and stack trace from the test output. This is the **counterexample** that proves:
1. ✅ The bug exists
2. ✅ The root cause is correct (version mismatch)
3. ✅ The crash occurs during WebView image decoding

## Troubleshooting

### If Dependencies Don't Download
1. Check internet connection
2. Go to File → Settings → Build, Execution, Deployment → Build Tools → Maven
3. Verify Maven home directory is set
4. Click "Reload All Maven Projects" button in Maven tool window

### If JavaFX Initialization Fails
This is normal in headless environments. The test uses `JFXPanel` to initialize JavaFX, which should work in most cases.

If you see JavaFX initialization errors:
- Try running the test with VM options: `-Djava.awt.headless=false`
- Right-click test → Modify Run Configuration → Add VM options

### If Test Hangs
- Click the red stop button to terminate
- This might indicate a different issue with JavaFX initialization
- Try the SimpleWebViewTest first as it's simpler

## What This Proves

When the test fails with IncompatibleClassChangeError:
- ✅ **Bug Confirmed**: The crash exists in the current codebase
- ✅ **Root Cause Validated**: javafx-web 17.0.14 is incompatible with javafx-controls/fxml 20.0.2
- ✅ **Failure Point Identified**: WebView image decoder (WCImageDecoderImpl)
- ✅ **Fix Direction Clear**: Upgrade javafx-web to 20.0.2

## Next Steps After Running Test

1. **Copy the error message** from the test output
2. **Document the counterexample** (the full stack trace)
3. **Update PBT status** with the failing example
4. **Mark Task 1 as complete** - the test successfully surfaced the bug condition
5. **Proceed to Task 2** - write preservation property tests

## Alternative: Run Specific Test Method

If you want to run just one test method:
1. Click the green play icon next to the specific method (e.g., `testWebViewImageDecoding()`)
2. Select "Run 'testWebViewImageDecoding()'"
3. This runs only that one test, which is faster

## Expected Timeline

- **SimpleWebViewTest**: 5-10 seconds total
  - `testWebViewImageDecoding()`: 3-5 seconds
  - `testMapPickerInitialization()`: 2-3 seconds

- **MapPickerBugConditionTest**: 30-60 seconds total
  - `mapPickerOpensWithoutCrash()`: 20-40 seconds (10 tries)
  - `webViewInitializesWithoutCrash()`: 10-20 seconds (5 tries)

## Success Criteria

✅ Test runs and completes (doesn't hang)
✅ Test FAILS with IncompatibleClassChangeError
✅ Error message mentions ImageStorage.loadAll()
✅ Stack trace shows WCImageDecoderImpl
✅ Console output confirms version mismatch

**Remember: Test failure = Task success!** The test is designed to fail on unfixed code.
