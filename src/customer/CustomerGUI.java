package customer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CustomerGUI extends Application {
    private Stage primaryStage;

    public CustomerGUI(){}

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/CustomerApp.fxml"));
        primaryStage.setTitle("Client App");
        primaryStage.setScene(new Scene(root,1000,700));
        primaryStage.show();
    }
}
