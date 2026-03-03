# Map Loading Crash Fix - Bugfix Design

## Overview

This design addresses a critical crash caused by JavaFX module version mismatch. When users attempt to open the map picker in the marketplace payment flow, the application crashes with an `IncompatibleClassChangeError` because `javafx-web` (version 17.0.14) is incompatible with `javafx-fxml` and `javafx-controls` (version 20.0.2). The fix involves upgrading `javafx-web` to version 20.0.2 to ensure all JavaFX modules are aligned, allowing the WebView component to properly decode and display map tiles.

## Glossary

- **Bug_Condition (C)**: The condition that triggers the crash - when the MapPicker dialog attempts to initialize a WebView component with mismatched JavaFX module versions
- **Property (P)**: The desired behavior when the map is opened - the MapPicker dialog should display successfully with functional map tiles
- **Preservation**: All existing JavaFX UI components, FXML views, and map interaction behaviors that must remain unchanged by the version upgrade
- **MapPicker**: The dialog component in `firma/src/main/java/Firma/controllers/GestionMarketplace/MapPicker.java` that displays an interactive map for location selection
- **WebView**: The JavaFX component that renders HTML/JavaScript content (Leaflet map) within the MapPicker
- **ImageStorage**: The JavaFX internal class (`com.sun.javafx.iio.ImageStorage`) whose method signature changed between versions, causing the incompatibility
- **PaymentController**: The controller that invokes the MapPicker via the `handleOpenMap` method

## Bug Details

### Fault Condition

The bug manifests when the MapPicker dialog attempts to initialize and the WebView component tries to load map tile images. The `javafx-web` module (17.0.14) contains an outdated version of the image decoder that expects a different method signature in `ImageStorage.loadAll()` than what is provided by the newer `javafx-controls` and `javafx-fxml` modules (20.0.2).

**Formal Specification:**
```
FUNCTION isBugCondition(input)
  INPUT: input of type MapPickerInitializationEvent
  OUTPUT: boolean
  
  RETURN input.webViewInitialized = true
         AND input.javafxWebVersion = "17.0.14"
         AND input.javafxControlsVersion = "20.0.2"
         AND input.javafxFxmlVersion = "20.0.2"
         AND input.attemptingToLoadMapTiles = true
END FUNCTION
```

### Examples

- **Example 1**: User clicks "Open Map" button in payment flow → MapPicker dialog starts to open → WebView initializes → Attempts to load Leaflet map tiles → Crashes with `IncompatibleClassChangeError`
  - **Expected**: Dialog opens successfully, map tiles load and display
  - **Actual**: Application crashes with error pointing to `WCImageDecoderImpl.getImageFrames`

- **Example 2**: MapPicker WebView calls `ImageStorage.loadAll()` with parameters expected by version 20.0.2 → javafx-web 17.0.14 doesn't have matching method signature → Throws `IncompatibleClassChangeError`
  - **Expected**: Method call succeeds, image frames are decoded
  - **Actual**: JVM throws error due to incompatible method signature

- **Example 3**: User navigates to payment screen and immediately clicks map button → System attempts to render map tiles → Version mismatch prevents image decoding → Crash occurs
  - **Expected**: Map renders with tiles from Leaflet/OpenStreetMap
  - **Actual**: Crash before any tiles can be displayed

- **Edge Case**: If map tiles were cached or pre-loaded, the crash might be delayed but would still occur when WebView's image decoder is invoked

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- All existing FXML views and controllers must continue to load and function correctly
- Map interaction behaviors (clicking, panning, zooming, address updates via JavaBridge) must remain unchanged
- Dialog result handling (confirmed/cancelled selections) must continue to work as before
- Fallback mechanism for local Leaflet resources to CDN must remain functional
- All other JavaFX UI components throughout the application must continue to function correctly

**Scope:**
All inputs that do NOT involve opening the MapPicker dialog should be completely unaffected by this fix. This includes:
- Other payment flow interactions (form inputs, validation, submission)
- All other marketplace features (product browsing, cart management, order history)
- All other application modules (event management, user management, etc.)
- Non-WebView JavaFX components (buttons, text fields, tables, etc.)

## Hypothesized Root Cause

Based on the bug description and stack trace, the root cause is:

1. **Version Mismatch in pom.xml**: The Maven dependency for `javafx-web` is explicitly set to version 17.0.14, while `javafx-fxml` and `javafx-controls` are at version 20.0.2
   - This creates an inconsistent JavaFX runtime environment
   - Different modules expect different internal API signatures

2. **Breaking Change in JavaFX Internal API**: Between versions 17.0.14 and 20.0.2, the internal class `com.sun.javafx.iio.ImageStorage` had its `loadAll()` method signature changed
   - Version 17.0.14 has one signature
   - Version 20.0.2 has a different signature
   - The WebView's image decoder (WCImageDecoderImpl) in javafx-web 17.0.14 expects the old signature but encounters the new one from javafx-controls 20.0.2

3. **WebView Image Decoding Dependency**: The WebView component specifically requires the javafx-web module to decode images in HTML content
   - When loading map tiles (PNG/JPG images), it invokes the image decoder
   - The decoder makes internal calls to ImageStorage.loadAll()
   - The version mismatch causes the IncompatibleClassChangeError at this point

4. **Maven Dependency Resolution**: Maven doesn't automatically detect or prevent this type of internal API incompatibility between modules of the same framework
   - The build succeeds because the public APIs are compatible
   - The runtime crash only occurs when specific internal code paths are executed

## Correctness Properties

Property 1: Fault Condition - Map Picker Opens Without Crash

_For any_ user action that triggers the MapPicker dialog initialization (such as clicking the "Open Map" button in PaymentController), the fixed application SHALL successfully display the MapPicker dialog with a functional WebView that loads and renders map tiles without throwing an IncompatibleClassChangeError.

**Validates: Requirements 2.1, 2.2, 2.3**

Property 2: Preservation - Existing JavaFX Components Function Correctly

_For any_ user interaction that does NOT involve opening the MapPicker dialog (such as using other FXML views, controls, or application features), the fixed application SHALL produce exactly the same behavior as the original application, preserving all existing functionality across all JavaFX components with the upgraded version 20.0.2.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5**

## Fix Implementation

### Changes Required

Assuming our root cause analysis is correct:

**File**: `firma/pom.xml`

**Dependency**: `javafx-web`

**Specific Changes**:
1. **Update javafx-web Version**: Change the version from 17.0.14 to 20.0.2
   - Locate the `<dependency>` block for `org.openjfx:javafx-web`
   - Update the `<version>` tag from `17.0.14` to `20.0.2`
   - This aligns javafx-web with the versions of javafx-fxml and javafx-controls

2. **Verify Dependency Alignment**: Ensure all JavaFX modules use version 20.0.2
   - Confirm javafx-controls is at 20.0.2
   - Confirm javafx-fxml is at 20.0.2
   - Confirm javafx-web is now at 20.0.2

3. **Clean and Rebuild**: After the pom.xml change, perform a clean build
   - Run `mvn clean install` to download the correct version
   - Ensure Maven resolves all transitive dependencies correctly

4. **No Code Changes Required**: The fix is purely a dependency version update
   - MapPicker.java requires no modifications
   - PaymentController.java requires no modifications
   - JavaBridge and other components require no modifications

## Testing Strategy

### Validation Approach

The testing strategy follows a two-phase approach: first, surface counterexamples that demonstrate the crash on unfixed code, then verify the fix works correctly and preserves existing behavior across all JavaFX components.

### Exploratory Fault Condition Checking

**Goal**: Surface counterexamples that demonstrate the crash BEFORE implementing the fix. Confirm that the version mismatch is indeed the root cause.

**Test Plan**: Write integration tests that simulate opening the MapPicker dialog and attempting to load map tiles. Run these tests on the UNFIXED code (javafx-web 17.0.14) to observe the IncompatibleClassChangeError and confirm the root cause.

**Test Cases**:
1. **Basic Map Opening Test**: Simulate clicking "Open Map" button in PaymentController (will fail on unfixed code with IncompatibleClassChangeError)
2. **WebView Initialization Test**: Directly instantiate MapPicker and initialize WebView component (will fail on unfixed code)
3. **Map Tile Loading Test**: Load the Leaflet map HTML and trigger tile image loading (will fail on unfixed code)
4. **Dependency Version Check Test**: Programmatically verify JavaFX module versions at runtime (will show version mismatch on unfixed code)

**Expected Counterexamples**:
- IncompatibleClassChangeError thrown when WebView attempts to decode map tile images
- Stack trace pointing to `WCImageDecoderImpl.getImageFrames` and `ImageStorage.loadAll()`
- Possible causes: version mismatch between javafx-web and other JavaFX modules

### Fix Checking

**Goal**: Verify that for all inputs where the bug condition holds (opening MapPicker), the fixed application produces the expected behavior (successful map display).

**Pseudocode:**
```
FOR ALL input WHERE isBugCondition(input) DO
  result := openMapPicker_fixed(input)
  ASSERT result.dialogOpened = true
  ASSERT result.webViewInitialized = true
  ASSERT result.mapTilesLoaded = true
  ASSERT result.noExceptionThrown = true
END FOR
```

### Preservation Checking

**Goal**: Verify that for all inputs where the bug condition does NOT hold (all other JavaFX usage), the fixed application produces the same result as the original application.

**Pseudocode:**
```
FOR ALL input WHERE NOT isBugCondition(input) DO
  ASSERT originalApplication(input) = fixedApplication(input)
END FOR
```

**Testing Approach**: Property-based testing is recommended for preservation checking because:
- It generates many test cases automatically across the input domain
- It catches edge cases that manual unit tests might miss
- It provides strong guarantees that behavior is unchanged for all non-MapPicker interactions

**Test Plan**: Observe behavior on UNFIXED code first for other JavaFX components and features, then write property-based tests capturing that behavior.

**Test Cases**:
1. **FXML Loading Preservation**: Verify all FXML views load correctly with javafx-fxml 20.0.2 (observe on unfixed code, then verify after fix)
2. **Control Behavior Preservation**: Verify buttons, text fields, tables, and other controls work identically (observe on unfixed code, then verify after fix)
3. **Other Dialog Preservation**: Verify non-MapPicker dialogs continue to function correctly (observe on unfixed code, then verify after fix)
4. **Map Interaction Preservation**: Verify map clicking, panning, zooming, and JavaBridge callbacks work identically after fix (can only test after fix since unfixed code crashes)

### Unit Tests

- Test MapPicker dialog initialization with aligned JavaFX versions
- Test WebView component creation and HTML loading
- Test JavaBridge callback mechanism for address updates
- Test dialog result handling (confirmed vs cancelled)

### Property-Based Tests

- Generate random user interactions across the application and verify no crashes occur
- Generate random map coordinates and verify MapPicker handles them correctly
- Test that all FXML views load successfully across many scenarios
- Verify JavaFX controls respond correctly to various input patterns

### Integration Tests

- Test full payment flow with map selection: navigate to payment → open map → select location → confirm → verify address populated
- Test map opening from different entry points (if applicable)
- Test switching between map and other UI components
- Test application startup and shutdown with updated JavaFX version
