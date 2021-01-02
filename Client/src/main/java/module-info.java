module pl.ROFS {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires org.bytedeco.opencv;
    requires org.bytedeco.javacv;
    requires webcam.capture;
    requires javafx.swing;
    requires java.xml.bind;
    requires javafx.graphics;
    requires com.jfoenix;

    opens pl.ROFS to javafx.fxml;
    exports pl.ROFS;
}