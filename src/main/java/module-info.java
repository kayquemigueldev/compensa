module com.kayque.compensa {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.kayque.compensa to javafx.fxml;
    opens com.kayque.compensa.profile.controller to javafx.fxml;

    exports com.kayque.compensa;
}