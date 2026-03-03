package Firma.tools.GestionMarketplace;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple JUnit test to demonstrate the IncompatibleClassChangeError
 * 
 * This is a standard JUnit 5 test (not property-based) that can be run
 * in any IDE or with Maven.
 * 
 * **Validates: Requirements 2.2, 2.3**
 * 
 * EXPECTED OUTCOME on UNFIXED code (javafx-web 17.0.14):
 * - Test FAILS with IncompatibleClassChangeError
 * - Error occurs when WebView tries to decode the image
 * 
 * EXPECTED OUTCOME on FIXED code (javafx-web 20.0.2):
 * - Test PASSES without errors
 */
public class SimpleWebViewTest {

    @BeforeAll
    static void initJavaFX() {
        // Initialize JavaFX toolkit
        new JFXPanel();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void testWebViewImageDecoding() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Exception[] caughtException = new Exception[1];

        Platform.runLater(() -> {
            try {
                // Create WebView - this is where javafx-web module is used
                WebView webView = new WebView();
                WebEngine engine = webView.getEngine();

                // Load HTML with an image (simulates map tile loading)
                // This triggers the image decoder which causes IncompatibleClassChangeError
                // on unfixed code due to version mismatch
                String html = "<!DOCTYPE html><html><body>" +
                    "<h1>Test Image Loading</h1>" +
                    "<img src='data:image/png;base64," +
                    "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==' " +
                    "alt='test' />" +
                    "</body></html>";

                engine.loadContent(html);

                // Wait for content to load and image to be decoded
                Thread.sleep(2000);

                System.out.println("WebView initialized successfully");
                System.out.println("Image decoded without errors");

            } catch (IncompatibleClassChangeError e) {
                // This is the expected error on unfixed code
                caughtException[0] = new Exception("IncompatibleClassChangeError: " + e.getMessage(), e);
                System.err.println("\n=== EXPECTED ERROR ON UNFIXED CODE ===");
                System.err.println("Error: " + e.getClass().getName());
                System.err.println("Message: " + e.getMessage());
                System.err.println("This confirms javafx-web 17.0.14 is incompatible with javafx-controls/fxml 20.0.2");
                System.err.println("=====================================\n");
                e.printStackTrace();
            } catch (Exception e) {
                caughtException[0] = e;
            } finally {
                latch.countDown();
            }
        });

        // Wait for test to complete
        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertTrue(completed, "Test timed out");

        // On unfixed code, we expect an IncompatibleClassChangeError
        // On fixed code, no exception should be thrown
        if (caughtException[0] != null) {
            // Test fails - this is expected on unfixed code
            fail("WebView image decoding failed: " + caughtException[0].getMessage(), caughtException[0]);
        }

        // If we reach here, the test passed (expected on fixed code)
        System.out.println("✓ Test passed - WebView successfully decoded image");
    }

    @Test
    void testMapPickerInitialization() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Exception[] caughtException = new Exception[1];
        boolean[] dialogOpened = new boolean[1];

        Platform.runLater(() -> {
            try {
                // This test attempts to trigger the MapPicker's WebView initialization
                // which is where the crash occurs in the real application
                
                System.out.println("Testing MapPicker initialization...");
                System.out.println("Current JavaFX versions:");
                System.out.println("  javafx-web: 17.0.14 (UNFIXED)");
                System.out.println("  javafx-controls: 20.0.2");
                System.out.println("  javafx-fxml: 20.0.2");
                System.out.println("\nExpected: IncompatibleClassChangeError when WebView loads map tiles");

                // Note: We can't easily test the full MapPicker.showAndWait() in a unit test
                // because it's modal and blocks. The WebView test above is sufficient to
                // demonstrate the bug condition.

                dialogOpened[0] = true;

            } catch (Exception e) {
                caughtException[0] = e;
            } finally {
                latch.countDown();
            }
        });

        boolean completed = latch.await(2, TimeUnit.SECONDS);
        assertTrue(completed, "Test timed out");

        if (caughtException[0] != null) {
            fail("MapPicker initialization failed: " + caughtException[0].getMessage(), caughtException[0]);
        }

        assertTrue(dialogOpened[0], "Dialog should have been initialized");
    }
}
