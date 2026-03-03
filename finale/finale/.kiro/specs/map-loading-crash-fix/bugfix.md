# Bugfix Requirements Document

## Introduction

This document addresses a critical crash that occurs when users attempt to open the map picker in the marketplace payment flow. The application crashes with an `IncompatibleClassChangeError` when the JavaFX WebView component tries to load map images/tiles. The root cause is a version mismatch between JavaFX modules in the project dependencies: `javafx-web` is at version 17.0.14 while `javafx-fxml` and `javafx-controls` are at version 20.0.2. This incompatibility prevents the WebView's image decoder from functioning correctly, blocking users from selecting delivery locations.

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN a user clicks to open the map in PaymentController (handleOpenMap method) THEN the system crashes with `java.lang.IncompatibleClassChangeError: Expected static method 'com.sun.javafx.iio.ImageFrame[] com.sun.javafx.iio.ImageStorage.loadAll(...)'`

1.2 WHEN the MapPicker dialog attempts to initialize the WebView component THEN the system fails to decode map tile images due to incompatible JavaFX module versions

1.3 WHEN the WebView tries to load Leaflet map tiles THEN the image decoder (WCImageDecoderImpl) cannot find the expected static method signature in the mismatched javafx-web module

### Expected Behavior (Correct)

2.1 WHEN a user clicks to open the map in PaymentController (handleOpenMap method) THEN the system SHALL successfully display the MapPicker dialog without crashing

2.2 WHEN the MapPicker dialog initializes the WebView component THEN the system SHALL successfully load and render map tiles using compatible JavaFX modules

2.3 WHEN the WebView loads Leaflet map tiles THEN the system SHALL decode and display images correctly using the properly versioned javafx-web module (20.0.2)

### Unchanged Behavior (Regression Prevention)

3.1 WHEN the MapPicker is used with valid address and city inputs THEN the system SHALL CONTINUE TO return the selected AddressResult with confirmed status

3.2 WHEN users interact with the map (clicking, panning, zooming) THEN the system SHALL CONTINUE TO update the address and city fields via the JavaBridge

3.3 WHEN users confirm or cancel the map selection THEN the system SHALL CONTINUE TO properly close the dialog and return the appropriate result

3.4 WHEN the MapPicker loads local Leaflet resources THEN the system SHALL CONTINUE TO fall back to CDN URLs if local resources are unavailable

3.5 WHEN other JavaFX components (controls, FXML views) are used throughout the application THEN the system SHALL CONTINUE TO function correctly with the updated JavaFX version
