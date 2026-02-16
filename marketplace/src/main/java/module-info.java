module com.example.marketplace {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires transitive javafx.graphics;
    requires java.desktop;
    requires java.sql;

    // Open GUI packages to JavaFX for FXML loading
    opens marketplace.GUI.Application to javafx.fxml, javafx.graphics;
    opens marketplace.GUI.Controller to javafx.fxml;

    // Export packages
    exports marketplace.GUI.Application;
    exports marketplace.GUI.Controller;
    exports marketplace.entities;
    exports marketplace.service;
    exports marketplace.tools;
}