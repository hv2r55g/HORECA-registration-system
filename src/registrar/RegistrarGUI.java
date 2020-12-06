package registrar;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import mixingProxy.MixingProxyGUIController;

public class RegistrarGUI extends Application {
    private Stage primaryStage;

    public RegistrarGUI(){}

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RegistrarApp.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Registrar App");
        primaryStage.setScene(new Scene(root,1400,700));
        RegistrarGUIController controller = loader.getController();
        controller.initController();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                //CODE ALS JE OP KRUISJE DRUKT
//                Platform.exit();
//                System.exit(0);
            }
        });
        primaryStage.show();
    }
}
