package customer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.Optional;

public class CustomerGUI extends Application {
    private Stage primaryStage;

    public CustomerGUI(){}

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Telefoonnummer");
        dialog.setHeaderText("Om de applicatie te starten is het noodzakelijk dat wij uw telefoonnummer hebben.");
        dialog.setContentText("Voer hiernaast uw telefoonnummer in:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            String telefoonr = result.get();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CustomerApp.fxml"));
            Parent root = loader.load();
            primaryStage.setTitle("App for: " + telefoonr);
            primaryStage.setScene(new Scene(root,1000,700));
            CustomerGUIController controller = loader.getController();
            controller.initController(telefoonr);
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent t) {
                    System.out.println("Goodbye: " + telefoonr);
                    Platform.exit();
                    System.exit(0);
                }
            });
            primaryStage.show();
        } else {
            System.out.println("U moet een telefoonnummer ingeven!");
            System.exit(-1);
        }

    }
}
