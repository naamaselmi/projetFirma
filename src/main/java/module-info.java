module com.examen.firmapi {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;

    opens com.examen.firmapi.controllers to javafx.fxml;
    exports com.examen.firmapi.app;
}
