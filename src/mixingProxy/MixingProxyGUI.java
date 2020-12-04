package mixingProxy;

import customer.CustomerGUIController;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MixingProxyGUI extends Application {
    private Stage primaryStage;

    public MixingProxyGUI(){}

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MixingProxyApp.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Mixing Proxy App");
        primaryStage.setScene(new Scene(root,1000,700));
        MixingProxyGUIController controller = loader.getController();
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
