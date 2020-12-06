package doctor;

import customer.Bezoek;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import matchingService.MatchingServiceInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DoctorGUIController extends UnicastRemoteObject implements Remote {
    @FXML
    TextField textFieldTel;

    @FXML
    Button buttonGetLogs;

    @FXML
    Button buttonToMatching;

    @FXML
    TableView tableViewLogs;


    private String currentPatient;
    private ObservableList<Bezoek> bezoekenPatient;
    private MatchingServiceInterface matchingServiceInterface;


    public DoctorGUIController() throws RemoteException {
        super();
    }
    public void initController() throws RemoteException, NotBoundException, MalformedURLException {
        initAttributen();
        initConnecties();
        initTable();
    }

    private void initTable() {
        TableColumn columnTimeEntered = new TableColumn("Time entered");
        columnTimeEntered.setMinWidth(200);
        columnTimeEntered.setCellValueFactory(new PropertyValueFactory<Bezoek,String>("timestampEnteredString"));
        columnTimeEntered.prefWidthProperty().bind(tableViewLogs.widthProperty().multiply(1.0/6.0));

        TableColumn columnTimeLeaving = new TableColumn("Time Left");
        columnTimeLeaving.setMinWidth(200);
        columnTimeLeaving.setCellValueFactory(new PropertyValueFactory<Bezoek,String>("timestampLeavingString"));
        columnTimeLeaving.prefWidthProperty().bind(tableViewLogs.widthProperty().multiply(1.0/6.0));

        TableColumn columnToken = new TableColumn("Token sign");
        columnToken.setMinWidth(200);
        columnToken.setCellValueFactory(new PropertyValueFactory<Bezoek,String>("tokenSign"));
        columnToken.prefWidthProperty().bind(tableViewLogs.widthProperty().multiply(1.0/6.0));

        TableColumn columnRandomIntBar = new TableColumn("Random int bar");
        columnRandomIntBar.setMinWidth(200);
        columnRandomIntBar.setCellValueFactory(new PropertyValueFactory<Bezoek,String>("randomIntBar"));
        columnRandomIntBar.prefWidthProperty().bind(tableViewLogs.widthProperty().multiply(1.0/6.0));

        TableColumn columnBusinessNumberBar = new TableColumn("Business number");
        columnBusinessNumberBar.setMinWidth(100);
        columnBusinessNumberBar.setCellValueFactory(new PropertyValueFactory<Bezoek,String>("businessNumberBar"));
        columnBusinessNumberBar.prefWidthProperty().bind(tableViewLogs.widthProperty().multiply(1.0/6.0));

        TableColumn columnHashBar = new TableColumn("Hash bar");
        columnHashBar.setMinWidth(200);
        columnHashBar.setCellValueFactory(new PropertyValueFactory<Bezoek,String>("hashBar"));
        columnHashBar.prefWidthProperty().bind(tableViewLogs.widthProperty().multiply(1.0/6.0));

        tableViewLogs.setItems(bezoekenPatient);
        tableViewLogs.getColumns().addAll(columnTimeEntered,columnTimeLeaving,columnToken,columnRandomIntBar,columnBusinessNumberBar,columnHashBar);
    }

    private void initConnecties() throws MalformedURLException, RemoteException, NotBoundException {
        //Connecten met de Matching server
        String hostname = "localhost";
        String clientService = "MatchingListening";
        String servicename = "MatchingServiceService";
        Naming.rebind("rmi://" + hostname + "/" + clientService, this);
        matchingServiceInterface = (MatchingServiceInterface) Naming.lookup("rmi://" + hostname + "/" + servicename);
    }

    private void initAttributen() {
        this.bezoekenPatient = FXCollections.observableArrayList();
    }

    @FXML
    private void stuurNaarMatching() throws RemoteException {
        System.out.println("size Bezoeken patient: " + bezoekenPatient.size());
        //OBSERVBLA MOET NAAR ARRAYLIST
        List<Bezoek> temp = new ArrayList<>();
        for (Bezoek bezoek: bezoekenPatient){
            temp.add(bezoek);
        }
        matchingServiceInterface.receiveInfectedBezoeken(temp);
        bezoekenPatient.clear();
    }

    @FXML
    private void leesBezoekenPatientIn() {
        if (textFieldTel.getText() == null || textFieldTel.getText().trim().isEmpty()){
            System.out.println("Voer een geldige waarde in de textfield!");
        } else {
            currentPatient = textFieldTel.getText();
            String path = "src/DoktersBestanden/";
            File currentFile = new File(path+ currentPatient +".csv");
            try {
                Scanner sc = new Scanner(currentFile);
                String firstLine = sc.nextLine();
                while (sc.hasNextLine()){
                    String currentBezoekString = sc.nextLine();
                    String[] gegevens = currentBezoekString.split(";");
                    Bezoek currentBezoek = new Bezoek(Long.parseLong(gegevens[0]),Long.parseLong(gegevens[1]),gegevens[2],gegevens[3],gegevens[4],gegevens[5]);
                    bezoekenPatient.add(currentBezoek);
                }
                System.out.println("Patient: " + currentPatient + " heeft de afgelopen tijd " + bezoekenPatient.size() + " keer een bezoek gebracht aan een bar of restaurant.");

            } catch (FileNotFoundException e) {
                System.out.println("De patient heeft nog geen dokterbestand aangemaakt");
            }
            buttonToMatching.setDisable(false);
        }

    }
}
