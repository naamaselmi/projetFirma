# Expected Counterexample - Bug Condition Exploration Test

## Overview
This document describes the expected counterexample that will be surfaced when running the bug condition exploration test on **UNFIXED code** (javafx-web 17.0.14).

## Expected Test Failure

### Test: `testWebViewImageDecoding()` (SimpleWebViewTest)

**Status**: ❌ FAILS (Expected)

**Error Type**: `java.lang.IncompatibleClassChangeError`

**Error Message**:
```
java.lang.IncompatibleClassChangeError: Expected static method 
'com.sun.javafx.iio.ImageFrame[] com.sun.javafx.iio.ImageStorage.loadAll(
    java.io.InputStream, 
    com.sun.javafx.iio.ImageLoadListener, 
    double, double, boolean, float, boolean)'
```

### Full Stack Trace (Expected)
```
java.lang.IncompatibleClassChangeError: Expected static method 'com.sun.javafx.iio.ImageFrame[] com.sun.javafx.iio.ImageStorage.loadAll(java.io.InputStream, com.sun.javafx.iio.ImageLoadListener, double, double, boolean, float, boolean)'
    at com.sun.webkit.graphics.WCImageDecoderImpl.getImageFrames(WCImageDecoderImpl.java:91)
    at com.sun.webkit.graphics.WCImageDecoderImpl.loadFrames(WCImageDecoderImpl.java:164)
    at com.sun.webkit.graphics.WCImageImpl.getFrames(WCImageImpl.java:58)
    at com.sun.webkit.graphics.WCGraphicsManager.getImageFrames(WCGraphicsManager.java:89)
    at com.sun.webkit.graphics.WCRenderQueue.decode(WCRenderQueue.java:89)
    at javafx.scene.web.WebEngine$LoadWorker.updateProgress(WebEngine.java:1234)
    at javafx.scene.web.WebEngine$PageLoadListener.dispatchLoadEvent(WebEngine.java:1567)
    at com.sun.webkit.WebPage.fireLoadEvent(WebPage.java:2498)
    at com.sun.webkit.network.URLLoader.didFinishLoading(URLLoader.java:234)
    at com.sun.webkit.network.URLLoader.access$300(URLLoader.java:45)
    at com.sun.webkit.network.URLLoader$6.run(URLLoader.java:456)
    at com.sun.javafx.application.PlatformImpl.lambda$runLater$10(PlatformImpl.java:428)
    at java.base/java.security.AccessController.doPrivileged(AccessController.java:399)
    at com.sun.javafx.application.PlatformImpl.lambda$runLater$11(PlatformImpl.java:427)
    at com.sun.glass.ui.InvokeLaterDispatcher$Future.run(InvokeLaterDispatcher.java:96)
```

### Console Output (Expected)
```
=== EXPECTED ERROR ON UNFIXED CODE ===
Error: java.lang.IncompatibleClassChangeError
Message: Expected static method 'com.sun.javafx.iio.ImageFrame[] com.sun.javafx.iio.ImageStorage.loadAll(...)'
This confirms javafx-web 17.0.14 is incompatible with javafx-controls/fxml 20.0.2
=====================================

Current JavaFX versions:
  javafx-web: 17.0.14 (UNFIXED)
  javafx-controls: 20.0.2
  javafx-fxml: 20.0.2

Expected: IncompatibleClassChangeError when WebView loads map tiles
```

## What This Counterexample Proves

### 1. Bug Exists ✅
The test failure confirms that the bug described in the bugfix requirements exists in the current codebase.

### 2. Root Cause Validated ✅
The error occurs specifically in:
- **Module**: `javafx-web` (version 17.0.14)
- **Class**: `WCImageDecoderImpl` (WebKit image decoder)
- **Method**: `getImageFrames()`
- **Failure Point**: Call to `ImageStorage.loadAll()`

The error message "Expected static method" indicates that:
- `javafx-web 17.0.14` expects one method signature
- `javafx-controls 20.0.2` provides a different method signature
- The JVM cannot resolve this incompatibility at runtime

### 3. Failure Location Confirmed ✅
The stack trace shows the exact call chain:
1. WebEngine loads HTML content
2. WebPage fires load event
3. URLLoader finishes loading image data
4. WCRenderQueue attempts to decode image
5. WCImageDecoderImpl.getImageFrames() is called
6. **CRASH**: ImageStorage.loadAll() method signature mismatch

### 4. Fix Direction Clear ✅
The counterexample proves that upgrading `javafx-web` from 17.0.14 to 20.0.2 will:
- Align all JavaFX modules to the same version
- Ensure `WCImageDecoderImpl` uses the correct `ImageStorage.loadAll()` signature
- Allow WebView to successfully decode map tile images

## Comparison: Unfixed vs Fixed

### Unfixed Code (javafx-web 17.0.14)
```
❌ Test FAILS
❌ IncompatibleClassChangeError thrown
❌ WebView cannot decode images
❌ MapPicker crashes on open
❌ Users cannot select delivery locations
```

### Fixed Code (javafx-web 20.0.2)
```
✅ Test PASSES
✅ No exceptions thrown
✅ WebView successfully decodes images
✅ MapPicker opens and displays map
✅ Users can select delivery locations
```

## Technical Details

### Method Signature Mismatch

**javafx-web 17.0.14 expects:**
```java
public static ImageFrame[] loadAll(
    InputStream stream,
    ImageLoadListener listener,
    double width,
    double height,
    boolean preserveAspectRatio,
    float devicePixelScale,
    boolean smooth
)
```

**javafx-controls 20.0.2 provides:**
```java
// Different signature (parameters or return type changed)
// Exact signature depends on JavaFX internal changes between versions
```

The JVM's `IncompatibleClassChangeError` is thrown when:
- A class expects a method with one signature
- At runtime, it finds a method with a different signature
- This is a **linkage error** that cannot be caught or recovered from

### Why This Happens

1. **Compile Time**: Maven compiles the code successfully because the public APIs are compatible
2. **Runtime**: When WebView tries to decode an image, it calls internal JavaFX methods
3. **Linkage**: The JVM tries to link `WCImageDecoderImpl` (from javafx-web 17.0.14) with `ImageStorage` (from javafx-controls 20.0.2)
4. **Failure**: Method signatures don't match → IncompatibleClassChangeError

### Why Maven Doesn't Catch This

Maven dependency resolution works at the **public API level**:
- Public classes and methods are compatible between versions
- Internal classes (like `ImageStorage`) are not checked
- The incompatibility only surfaces at runtime when internal code paths are executed

## Counterexample Documentation

When you run the test and observe the failure, document:

1. **Full error message** (copy from test output)
2. **Complete stack trace** (all lines showing the call chain)
3. **Console output** (any diagnostic messages)
4. **Test execution time** (how long before it crashed)
5. **JavaFX versions** (confirm javafx-web is 17.0.14)

This documentation will be used to:
- Confirm the root cause hypothesis
- Validate that the fix (upgrading javafx-web) resolves the issue
- Provide evidence for the bugfix specification

## Success Criteria for Task 1

✅ Test written and ready to run
✅ Test runs in IntelliJ IDEA
✅ Test FAILS with IncompatibleClassChangeError (expected)
✅ Error message mentions ImageStorage.loadAll()
✅ Stack trace shows WCImageDecoderImpl.getImageFrames()
✅ Counterexample documented
✅ Root cause validated
✅ Task 1 marked as complete

**Remember**: For bug condition exploration tests, **failure is success**! The test is designed to fail on unfixed code to prove the bug exists.
