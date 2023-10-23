module com.example.cadastrojavafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.cadastrojavafx to javafx.fxml;
    exports com.example.cadastrojavafx;
    exports model;
}