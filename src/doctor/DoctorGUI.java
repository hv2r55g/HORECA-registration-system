package doctor;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class DoctorGUI extends Application {
    private Stage primaryStage;

    public DoctorGUI(){

    }

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DoctorApp.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Doctor App");
        primaryStage.setScene(new Scene(root,1400,700));
        DoctorGUIController controller = loader.getController();
        controller.initController();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
        primaryStage.show();
    }
}
