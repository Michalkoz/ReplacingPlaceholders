module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
//    requires xml.apis;
    requires java.xml;

    opens com.example.demo to javafx.fxml;
    exports com.example.demo;
}