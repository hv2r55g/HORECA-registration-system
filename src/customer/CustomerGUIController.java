package customer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;



public class CustomerGUIController {

    @FXML
    Button button1;

    @FXML
    Button button2;

    public void click(){
        System.out.println("Button geklikt");
    }

}
