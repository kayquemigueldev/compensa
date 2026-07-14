module com.kayque.compensa {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    opens com.kayque.compensa to javafx.fxml;
    opens com.kayque.compensa.profile.controller to javafx.fxml;
    opens com.kayque.compensa.purchase.controller to javafx.fxml;
    opens com.kayque.compensa.history.controller to javafx.fxml;
    opens com.kayque.compensa.wishlist.controller to javafx.fxml;
    opens com.kayque.compensa.dashboard.controller to javafx.fxml;

    exports com.kayque.compensa;
}