package Firma.tools.GestionMarketplace;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;
import net.jqwik.api.*;
import net.jqwik.api.constraints.StringLength;
import org.junit.jupiter.api.BeforeAll;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Bug Condition Exploration Test for MapPicker Crash
 * 
 * **Validates: Requirements 2.1, 2.2, 2.3**
 * 
 * CRITICAL: This test MUST FAIL on unfixed code (javafx-web 17.0.14) - failure confirms the bug exists.
 * 
 * Root Cause: JavaFX version mismatch between javafx-web 17.0.14 and javafx-controls/fxml 20.0.2
 * causes IncompatibleClassChangeError when WebView tries to decode map tile images.
 * 
 * Expected Behavior on UNFIXED code:
 * - Test FAILS with IncompatibleClassChangeError
 * - Stack trace shows WCImageDecoderImpl.getImageFrames and ImageStorage.loadAll() incompatibility
 * - Error message: "Expected static method 'com.sun.javafx.iio.ImageFrame[] com.sun.javafx.iio.ImageStorage.loadAll(...)'"
 * 
 * Expected Behavior on FIXED code (after javafx-web upgraded to 20.0.2):
 * - Test PASSES - MapPicker opens successfully, WebView initializes, map tiles load
 * 
 * HOW TO RUN:
 * mvn clean test -Dtest=MapPickerBugConditionTest
 * 
 * OR with jqwik specific configuration:
 * mvn clean test -Dtest=MapPickerBugConditionTest -Djqwik.tries=10
 */
public class MapPickerBugConditionTest {

    @BeforeAll
    static void initJavaFX() {
        // Initialize JavaFX toolkit (required for headless testing)
        try {
            new JFXPanel(); // This initializes JavaFX toolkit
            Thread.sleep(500); // Give toolkit time to initialize
        } catch (Exception e) {
            System.err.println("JavaFX initialization warning: " + e.getMessage());
        }
    }

    /**
     * Property 1: Fault Condition - Map Picker Opens Without Crash
     * 
     * This property tests the concrete failing case: opening MapPicker with javafx-web 17.0.14
     * and javafx-controls/fxml 20.0.2 should trigger IncompatibleClassChangeError.
     * 
     * On UNFIXED code: This test will FAIL (expected - proves bug exists)
     * On FIXED code: This test will PASS (confirms fix works)
     */
    @Property(tries = 10)
    @Label("MapPicker opens without IncompatibleClassChangeError")
    void mapPickerOpensWithoutCrash(
            @ForAll @StringLength(min = 0, max = 100) String initialAddress,
            @ForAll @StringLength(min = 0, max = 50) String initialCity) {
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> caughtException = new AtomicReference<>();
        AtomicReference<Boolean> dialogOpened = new AtomicReference<>(false);

        Platform.runLater(() -> {
            try {
                // Create a Stage for the dialog owner
                Stage ownerStage = new Stage();
                
                // Create MapPicker instance
                MapPicker mapPicker = new MapPicker();
                
                // Attempt to show the dialog - this triggers WebView initialization
                // On unfixed code (javafx-web 17.0.14), this will throw IncompatibleClassChangeError
                // when WebView tries to load map tiles and decode images
                
                // We'll use a separate thread to show the dialog and close it quickly
                new Thread(() -> {
                    try {
                        Thread.sleep(2000); // Wait 2 seconds for WebView to initialize and load tiles
                        Platform.runLater(() -> {
                            // Close any open dialogs
                            Stage.getWindows().stream()
                                .filter(window -> window instanceof Stage)
                                .filter(Stage.class::isInstance)
                                .map(Stage.class::cast)
                                .filter(stage -> stage.getTitle() != null && 
                                       stage.getTitle().contains("Sélectionner une adresse"))
                                .forEach(stage -> {
                                    dialogOpened.set(true);
                                    stage.close();
                                });
                        });
                    } catch (Exception e) {
                        caughtException.set(e);
                    }
                }, "dialog-closer").start();
                
                // This call will block until dialog is closed or crashes
                mapPicker.showAndWait(ownerStage, initialAddress, initialCity);
                
                // If we reach here without exception, the dialog opened successfully
                ownerStage.close();
                
            } catch (IncompatibleClassChangeError e) {
                // This is the expected error on unfixed code
                caughtException.set(e);
                System.err.println("EXPECTED ERROR on unfixed code: " + e.getClass().getName());
                System.err.println("Message: " + e.getMessage());
                System.err.println("This confirms the bug exists - javafx-web 17.0.14 is incompatible with javafx-controls/fxml 20.0.2");
            } catch (Throwable t) {
                // Catch any other exceptions
                caughtException.set(t);
            } finally {
                latch.countDown();
            }
        });

        try {
            // Wait for the test to complete (max 10 seconds)
            boolean completed = latch.await(10, TimeUnit.SECONDS);
            
            if (!completed) {
                throw new AssertionError("Test timed out - dialog may have hung");
            }

            Throwable exception = caughtException.get();
            
            if (exception != null) {
                // On unfixed code, we expect IncompatibleClassChangeError
                if (exception instanceof IncompatibleClassChangeError) {
                    // This is the bug we're testing for - throw it to fail the test
                    // Failure confirms the bug exists
                    throw new AssertionError(
                        "EXPECTED FAILURE on unfixed code: IncompatibleClassChangeError detected. " +
                        "This confirms javafx-web 17.0.14 is incompatible with javafx-controls/fxml 20.0.2. " +
                        "Error: " + exception.getMessage(), 
                        exception);
                } else {
                    // Unexpected exception type
                    throw new AssertionError("Unexpected exception: " + exception.getMessage(), exception);
                }
            }
            
            // If no exception was thrown, the dialog opened successfully
            // This is the expected behavior on FIXED code (javafx-web 20.0.2)
            // On unfixed code, this test should not reach here
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Test interrupted", e);
        }
    }

    /**
     * Simplified test that directly tests WebView initialization
     * This is more focused on the exact failure point
     */
    @Property(tries = 5)
    @Label("WebView initializes without IncompatibleClassChangeError")
    void webViewInitializesWithoutCrash() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> caughtException = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                // Directly create a WebView - this is where the version mismatch causes issues
                javafx.scene.web.WebView webView = new javafx.scene.web.WebView();
                javafx.scene.web.WebEngine engine = webView.getEngine();
                
                // Load HTML content with an image (simulates map tile loading)
                // On unfixed code, this will trigger IncompatibleClassChangeError when
                // the image decoder tries to decode the image
                String htmlWithImage = "<!DOCTYPE html><html><body>" +
                    "<img src='data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==' />" +
                    "</body></html>";
                
                engine.loadContent(htmlWithImage);
                
                // Wait for content to load
                Thread.sleep(1500);
                
            } catch (IncompatibleClassChangeError e) {
                caughtException.set(e);
                System.err.println("EXPECTED ERROR on unfixed code: " + e.getClass().getName());
                System.err.println("Message: " + e.getMessage());
            } catch (Throwable t) {
                caughtException.set(t);
            } finally {
                latch.countDown();
            }
        });

        try {
            boolean completed = latch.await(5, TimeUnit.SECONDS);
            
            if (!completed) {
                throw new AssertionError("Test timed out");
            }

            Throwable exception = caughtException.get();
            
            if (exception != null) {
                if (exception instanceof IncompatibleClassChangeError) {
                    throw new AssertionError(
                        "EXPECTED FAILURE on unfixed code: IncompatibleClassChangeError in WebView image decoding. " +
                        "Error: " + exception.getMessage(), 
                        exception);
                } else {
                    throw new AssertionError("Unexpected exception: " + exception.getMessage(), exception);
                }
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Test interrupted", e);
        }
    }
}
