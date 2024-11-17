module org.example.lms {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.desktop;
    requires java.sql;

    opens org.example.lms to javafx.fxml;
    exports org.example.lms;
}