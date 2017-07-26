package app;

import javafx.fxml.FXML;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class Controller {

    @FXML
    private TextField inputText;

    @FXML
    private TextArea outputField;

    @FXML
    private URL location;

    @FXML
    private ResourceBundle resources;

    public Controller(){

    }

    @FXML
    private void initialize(){

    }

    @FXML
    private void printOutput(){
        outputField.setText(inputText.getText());
    }

}
