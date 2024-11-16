module org.example.lms {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens org.example.lms to javafx.fxml;
    exports org.example.lms;
}