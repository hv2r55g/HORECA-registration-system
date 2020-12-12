package customer;

import bar.QRCode;
import doctor.CriticalTuple;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import matchingService.MatchingServiceInterface;
import javafx.util.Duration;
import mixingProxy.Capsule;
import mixingProxy.MixingProxyInterface;
import registrar.RegistrarInterface;
import registrar.Token;

import javax.swing.*;
import java.io.*;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;


public class CustomerGUIController extends UnicastRemoteObject implements Remote {
    //--------------------------------------------------------FXML------------------------------------------------------------------------------//
    @FXML
    TextField inputDatastring;

    @FXML
    Label labelBusinessNumber;

    @FXML
    Label labelRandomInt;

    @FXML
    Label labelHashBar;

    @FXML
    Button buttonBezoekBar;

    @FXML
    Button buttonVerlaatBar;

    @FXML
    Button buttonClearDatabase;

    @FXML
    ImageView imageViewSign;

    @FXML
    Button buttonCOVID;

    @FXML
    TableView tableViewBezoeken;

    //------------------------------------------------------------------------------------------------------------------------------------------//

    //--------------------------------------------------------ATTRIBUTEN------------------------------------------------------------------------//
    private String phonenumber;
    private List<Token> tokens;
    private Token currentToken;
    private ObservableList<Bezoek> bezoeken;
    private List<Bezoek> bezoekenLaatsteZevenDagen;
    private QRCode QRcodeCurrentBar;
    private RegistrarInterface registrarInterface;
    private MixingProxyInterface mixingProxyInterface;
    private MatchingServiceInterface matchingServiceInterface;
    //------------------------------------------------------------------------------------------------------------------------------------------//

    public CustomerGUIController() throws RemoteException {
        super();
    }

    private void initAttributen(){
        this.bezoeken = leesLocalDatabase();
    }

    private List<Bezoek> getBezoekenLaatsteZevenDagen(){
        List<Bezoek> bezoekenAfgelopenWeek = new ArrayList<>();
        long DAY_IN_MS = 1000 * 60 * 60 * 24;
        long currentTime = System.currentTimeMillis();
        long timeSevenDaysAgo = currentTime - (7 * DAY_IN_MS);

        for (Bezoek bezoek : bezoeken){
            if (timeSevenDaysAgo <= bezoek.getCapsule().getTimestampEntered()){
                bezoekenAfgelopenWeek.add(bezoek);
            }
        }
        return bezoekenAfgelopenWeek;
    }

    private ObservableList<Bezoek> leesLocalDatabase(){
        ObservableList<Bezoek> result = FXCollections.observableArrayList();
        String path = "src/DoktersBestanden/";
        String fileName = phonenumber + ".csv";
        File file = new File(path+fileName);
        try {
            Scanner sc = new Scanner(file);
            if (sc.hasNextLine()){
                String firstLine = sc.nextLine();
                while (sc.hasNextLine()){
                    String[] bezoek = sc.nextLine().split(";");
                    Bezoek nieuwBezoek = new Bezoek(Long.parseLong(bezoek[0]),Long.parseLong(bezoek[1]),bezoek[2],bezoek[3],bezoek[4],bezoek[5]);
                    //System.out.println(nieuwBezoek);
                    result.add(nieuwBezoek);
                }
            } else {
                System.out.println("DB is gecleared geweest");
            }
        } catch (FileNotFoundException e) {
            System.out.println("Nieuwe gebruiker");
        }
        return result;
    }

    private void initConnecties(){
        try {
            //CONNECTEN MET REGISTRAR
            String hostname = "localhost";
            String clientService = "RegistrarListening";
            String servicename = "RegistrarService";
            Naming.rebind("rmi://" + hostname + "/" + clientService, this);
            registrarInterface = (RegistrarInterface) Naming.lookup("rmi://" + hostname + "/" + servicename);

            //CONNECTEN MET MIXING PROXY
            clientService = "MixingProxyListening";
            servicename = "MixingProxyService";
            Naming.rebind("rmi://" + hostname + "/" + clientService, this);
            mixingProxyInterface = (MixingProxyInterface) Naming.lookup("rmi://" + hostname + "/" + servicename);

            //CONNECTEN MET MATCHING SERVICE
            clientService = "MatchingServiceListening";
            servicename = "MatchingServiceService";
            Naming.rebind("rmi://" + hostname + "/" + clientService, this);
            matchingServiceInterface = (MatchingServiceInterface) Naming.lookup("rmi://" + hostname + "/" + servicename);


        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initTable() {
        TableColumn columnTimeEntered = new TableColumn("Time entered");
        columnTimeEntered.setMinWidth(200);
        columnTimeEntered.setCellValueFactory(new PropertyValueFactory<Bezoek,String>("timestampEnteredString"));
        columnTimeEntered.prefWidthProperty().bind(tableViewBezoeken.widthProperty().multiply(1.0/6.0));

        TableColumn columnTimeLeaving = new TableColumn("Time Left");
        columnTimeLeaving.setMinWidth(200);
        columnTimeLeaving.setCellValueFactory(new PropertyValueFactory<Bezoek,String>("timestampLeavingString"));
        columnTimeLeaving.prefWidthProperty().bind(tableViewBezoeken.widthProperty().multiply(1.0/6.0));

        TableColumn columnToken = new TableColumn("Token sign");
        columnToken.setMinWidth(200);
        columnToken.setCellValueFactory(new PropertyValueFactory<Bezoek,String>("tokenSign"));
        columnToken.prefWidthProperty().bind(tableViewBezoeken.widthProperty().multiply(1.0/6.0));

        TableColumn columnRandomIntBar = new TableColumn("Random int bar");
        columnRandomIntBar.setMinWidth(200);
        columnRandomIntBar.setCellValueFactory(new PropertyValueFactory<Bezoek,String>("randomIntBar"));
        columnRandomIntBar.prefWidthProperty().bind(tableViewBezoeken.widthProperty().multiply(1.0/6.0));

        TableColumn columnBusinessNumberBar = new TableColumn("Business number");
        columnBusinessNumberBar.setMinWidth(100);
        columnBusinessNumberBar.setCellValueFactory(new PropertyValueFactory<Bezoek,String>("businessNumberBar"));
        columnBusinessNumberBar.prefWidthProperty().bind(tableViewBezoeken.widthProperty().multiply(1.0/6.0));

        TableColumn columnHashBar = new TableColumn("Hash bar");
        columnHashBar.setMinWidth(200);
        columnHashBar.setCellValueFactory(new PropertyValueFactory<Bezoek,String>("hashBar"));
        columnHashBar.prefWidthProperty().bind(tableViewBezoeken.widthProperty().multiply(1.0/6.0));

        tableViewBezoeken.setItems(bezoeken);
        tableViewBezoeken.getColumns().addAll(columnTimeEntered,columnTimeLeaving,columnToken,columnRandomIntBar,columnBusinessNumberBar,columnHashBar);
    }

    public void initController(String telefoonr) throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidKeySpecException {
        this.phonenumber = telefoonr;
        initAttributen();
        initConnecties();
        initTable();
        //1 AANVRAAG (VAN 48 TOKENS) PER DAG, IEDERE TOKEN KAN MAAR 1 KEER GEBRUIKT WORDEN
        requestTokens();

    }

    @FXML
    private void scanQRCode(){
        String dataString = inputDatastring.getText();
        String[] temp = dataString.split(";");
        QRcodeCurrentBar = new QRCode(temp[0],temp[1],temp[2]);
        labelRandomInt.setText(QRcodeCurrentBar.getRandomGetal());
        labelBusinessNumber.setText(QRcodeCurrentBar.getBusinessNumber());
        labelHashBar.setText(String.valueOf(QRcodeCurrentBar.getHashBar()));
        inputDatastring.clear();
    }

    @FXML
    private void clearLocalDatabase() throws IOException {
        System.out.println("Clear local database");
        String path = "src/DoktersBestanden/";
        String fileName = phonenumber + ".csv";

        FileWriter fileWritter = new FileWriter(path+fileName);
        BufferedWriter bw = new BufferedWriter(fileWritter);
        bw.write("");
        bw.close();

        bezoeken.clear();
    }

    @FXML
    private void bezoekBar() throws RemoteException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        if (!tokens.isEmpty()) {
            //CAPSULE OPMAKEN
            currentToken = tokens.get(0);
            //System.out.println("Dit is de hashBar die we naar de mixing sturen: " + QRcodeCurrentBar.getHashBar());
            Capsule capsule = new Capsule(currentToken, QRcodeCurrentBar.getHashBar());

            boolean doSign = mixingProxyInterface.sendCapsule(capsule);
            if (doSign) {
                //dan moet men de sign ontvangen
                String titelLogo = mixingProxyInterface.signCapsule(capsule);

                //LOGO INLADEN
                setLogo(titelLogo);
            } else {
                System.out.println("bezoek gefailed, waarschijnlijk door een check");
            }
            //GEBRUIKT TOKEN VERWIJDEREN
            tokens.remove(0);
        } else {
            System.out.println("U kan deze bar helaas niet meer bezoeken, uw tokens voor vandaag zijn verbruikt");
        }
    }

    private void setLogo(String titelLogo) {
        String path = "src/Resources/Icons/" + titelLogo;
        File file = new File(path);
        try {
            Image image = new Image(new FileInputStream(file));
            imageViewSign.setImage(image);
        } catch (FileNotFoundException e) {
            System.out.println("Image niet gevonden in de resource folder");
        }
    }

    @FXML
    private void verlaatBar() throws RemoteException, FileNotFoundException {
        //LEAVING TIME IN CAPSULE GAAN FIXEN, TERGELIJK NOG KEER DAT OBECT TERUGSTUREN VOOR ONS BEZOEK AAN TE MAKEN
        Capsule currentCapsule = mixingProxyInterface.requestLeaving(currentToken);
        //BEZOEK LOKAAL OPSLAAN
        Bezoek bezoek = new Bezoek(QRcodeCurrentBar.getRandomGetal(),QRcodeCurrentBar.getBusinessNumber(),currentCapsule);
        bezoeken.add(bezoek);   //hoeft niet mer perse
        sendToLocalDatabase(bezoek);
        labelHashBar.setText("");
        labelBusinessNumber.setText("");
        labelRandomInt.setText("");
        imageViewSign.setImage(new Image(new FileInputStream(new File("src/Resources/Icons/default.jpg"))));
    }

    @FXML
    private void amIInfected() throws RemoteException {
        System.out.println("amIInfected knop pushed");
        //STAP 1: CRITICAL TUPLES OPHALEN
        List<CriticalTuple> criticalTuples = matchingServiceInterface.requestCriticalTuples();
        List<Capsule> geinfecteerdeCapsules = new ArrayList<>();

        System.out.println("De size van critical tuples: " + criticalTuples.size());
        for (CriticalTuple currentCriticalTuple: criticalTuples){
            //STAP 2: KIJKEN OF ER OVERLAP IS TUSSEN DE TUPLES EN ONZE BEZOEKEN
            //BEZOEKEN OPHALEN DIE GLEIJKE HASH HEBBEN
            List<Bezoek> bezoekenUitInfectedBar = new ArrayList<>();
            for (Bezoek bezoek: bezoeken){
                if (bezoek.getCapsule().getHashBar().equals(currentCriticalTuple.getHashBar())){
                    bezoekenUitInfectedBar.add(bezoek);
                }
            }

            //STAP 3: KIJKEN WELKE BEZOEKEN ER OVERLAP IS, BEST METHODE IS OVERLAP MAKEN. BIJ CAPSULE HAD IK DAT AL GEMAAKT MAAR MOET DAAR NIET STAAN
            if (!bezoekenUitInfectedBar.isEmpty()){
                for (Bezoek bezoek: bezoekenUitInfectedBar){
                    if (bezoek.getCapsule().isErOverlap(currentCriticalTuple)){
                        System.out.println("Dit is de infected token: " + bezoek.getCapsule().getTokenCustomer().getSignature());
                        geinfecteerdeCapsules.add(bezoek.getCapsule());
                        setLogo("CovidContact.jpg");
                    }
                }
            }
        }

        if (geinfecteerdeCapsules.size()==0){
            setLogo("CovidProof.jpg");
        }
        //STAP 4: lIJST VAN GEINFECTEERDE DOORSTUREN NAAR MIXING, MISSCHIEN VOOR DE DUIDELIJKHEID NIEUWE INTERFACE METHODE
        mixingProxyInterface.sendACK(geinfecteerdeCapsules);
    }

    private void requestTokens() throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        tokens = registrarInterface.requestDailyCustomerToken(phonenumber);
    }

    private void sendToLocalDatabase(Bezoek bezoek){
        try {
            System.out.println("Voeg bezoek toe aan local database");
            String path = "src/DoktersBestanden/";
            String fileName = phonenumber + ".csv";
            File file = new File(path+fileName);

            FileWriter fileWritter = new FileWriter(path+fileName,true);
            BufferedWriter bw = new BufferedWriter(fileWritter);
            Scanner sc = new Scanner(file);

            if(!sc.hasNextLine()) {
                //System.out.println("File bestaat nog niet");
                bw.append("Timestamp entering;Timestamp leaving;Token sign;Random number bar;Bussiness number bar;Hash bar");
                bw.newLine();
                bw.append(bezoek.getCapsule().getTimestampEntered() + ";" + bezoek.getCapsule().getTimestampLeaving()+ ";"+ bezoek.getCapsule().getTokenCustomer().getSignature() + ";" + bezoek.getRandomIntBar()+ ";" + bezoek.getBusinessNumberBar()+ ";" + bezoek.getCapsule().getHashBar());
                bw.newLine();
            } else {
                //System.out.println("File bestaat wel al");
                bw.append(bezoek.getCapsule().getTimestampEntered() + ";" + bezoek.getCapsule().getTimestampLeaving()+ ";"+ bezoek.getCapsule().getTokenCustomer().getSignature() + ";" + bezoek.getRandomIntBar()+ ";" + bezoek.getBusinessNumberBar()+ ";" + bezoek.getCapsule().getHashBar());
                bw.newLine();
            }
            sc.close();
            bw.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }


}
