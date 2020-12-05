package customer;

import bar.QRCode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import matchingService.MatchingServiceInterface;
import mixingProxy.Capsule;
import mixingProxy.MixingProxyInterface;
import registrar.RegistrarInterface;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;


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
    private List<byte[]> tokens;
    private byte[] currentToken;
    private ObservableList<Bezoek> bezoeken;
    private List<Bezoek> bezoekenLaatsteZevenDagen;
    private QRCode QRcodeCurrentBar;
    private Map<String,String> mappingIcons;
    private RegistrarInterface registrarInterface;
    private MixingProxyInterface mixingProxyInterface;
    private MatchingServiceInterface matchingServiceInterface;
    //------------------------------------------------------------------------------------------------------------------------------------------//

    public CustomerGUIController() throws RemoteException {
        super();
    }

    private void initAttributen(){
        this.bezoeken = leesLocalDatabase();
        this.mappingIcons = new HashMap<>();
    }

    private List<Bezoek> getBezoekenLaatsteZevenDagen(){
        List<Bezoek> bezoekenAfgelopenWeek = new ArrayList<>();
        long DAY_IN_MS = 1000 * 60 * 60 * 24;
        long currentTime = System.currentTimeMillis();
        long timeSevenDaysAgo = currentTime - (7 * DAY_IN_MS);

        for (Bezoek bezoek : bezoeken){
            if (timeSevenDaysAgo <= bezoek.getTimestampEntered()){
                bezoekenAfgelopenWeek.add(bezoek);
            }
        }
        return bezoekenAfgelopenWeek;
    }

    //Deze methode is het resultaat dat de knop COVID moet teruggeven
    private boolean getInContactGekomenMetBesmetPersoon() throws RemoteException {
        return matchingServiceInterface.requestInfectedOrNot(bezoekenLaatsteZevenDagen);
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
                    result.add(new Bezoek(Long.parseLong(bezoek[0]),Long.parseLong(bezoek[1]),bezoek[2],bezoek[3],bezoek[4].getBytes()));
                }
            } else {
                System.out.println("DB is gecleared geweest");
            }
        } catch (FileNotFoundException e) {
            System.out.println("Nieuwe gebruiker");
        }
        return result;
    }

    private void initLogos(){
        //1 KEER PER DAG BEIDE LIJSTEN SHUFFELEN? KWEET ALLEEN NIET GOED HOE IK HET MOET IMPLEMENTEREN
        //VOORLOPIG GEWOON INDEXEN GELIJK STELLEN AAN ELKAAR
        String[] interval = {"0-4","5-9","10-14","15-19","20-24","25-29","30-34","35-39","40-44","45-49","50-54","55-59","60-64","65-69","70-74","75-79","80-84","85-89","90-94","95-99",};
        String[] afbeeldingen = {"BlueDot.jpg","BlueSquare.jpg","BlueStar.jpg","BlueTriangle.jpg","BlueHeart.jpg",
                "YellowDot.jpg","YellowSquare.jpg","YellowStar.jpg","YellowTriangle.jpg", "YellowHeart.jpg",
                "GreenDot.jpg","GreenSquare.jpg","GreenStar.jpg","GreenTriangle.jpg","GreenHeart.jpg",
                "RedDot.jpg","RedSquare.jpg","RedStar.jpg","RedTriangle.jpg","RedHeart.jpg"};
        //SHUFFELEN
        List<String> intervalArray = Arrays.asList(interval);
        List<String> afbeeldingenArray = Arrays.asList(afbeeldingen);
        //Collections.shuffle(intervalArray);
        //Collections.shuffle(afbeeldingenArray);

        boolean evenGroot = interval.length == afbeeldingen.length;
        System.out.println("Zijn de sizes even groot? " + evenGroot);
        if (evenGroot){
            for (int i = 0; i < interval.length; i++) {
                mappingIcons.put(intervalArray.get(i),afbeeldingenArray.get(i));
            }
        }
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
            servicename = "MatchingService";
            Naming.rebind("rmi://" + hostname + "/" + clientService, this);
            matchingServiceInterface = (MatchingServiceInterface) Naming.lookup("rmi://" + hostname + "/" + servicename);


        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initTable() {
        TableColumn columnTimeEntered = new TableColumn("Time entered");
        columnTimeEntered.setMinWidth(000);
        columnTimeEntered.setCellValueFactory(new PropertyValueFactory<Bezoek,String>("timestampEnteredString"));
        columnTimeEntered.prefWidthProperty().bind(tableViewBezoeken.widthProperty().multiply(0.2));

        TableColumn columnTimeLeaving = new TableColumn("Time Left");
        columnTimeLeaving.setMinWidth(200);
        columnTimeLeaving.setCellValueFactory(new PropertyValueFactory<Bezoek,String>("timestampLeavingString"));
        columnTimeLeaving.prefWidthProperty().bind(tableViewBezoeken.widthProperty().multiply(0.2));

        TableColumn columnRandomIntBar = new TableColumn("Random int bar");
        columnRandomIntBar.setMinWidth(200);
        columnRandomIntBar.setCellValueFactory(new PropertyValueFactory<Bezoek,String>("randomIntBar"));
        columnRandomIntBar.prefWidthProperty().bind(tableViewBezoeken.widthProperty().multiply(0.2));

        TableColumn columnBusinessNumberBar = new TableColumn("Business number");
        columnBusinessNumberBar.setMinWidth(200);
        columnBusinessNumberBar.setCellValueFactory(new PropertyValueFactory<Bezoek,String>("businessNumberBar"));
        columnBusinessNumberBar.prefWidthProperty().bind(tableViewBezoeken.widthProperty().multiply(0.2));

        TableColumn columnHashBar = new TableColumn("Hash bar");
        columnHashBar.setMinWidth(200);
        columnHashBar.setCellValueFactory(new PropertyValueFactory<Bezoek,String>("hashBar"));
        columnHashBar.prefWidthProperty().bind(tableViewBezoeken.widthProperty().multiply(0.2));

        tableViewBezoeken.setItems(bezoeken);
        tableViewBezoeken.getColumns().addAll(columnTimeEntered,columnTimeLeaving,columnRandomIntBar,columnBusinessNumberBar,columnHashBar);
    }

    public void initController(String telefoonr) throws RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidKeySpecException {
        this.phonenumber = telefoonr;
        initAttributen();
        initLogos();
        initConnecties();
        initTable();
        //1 AANVRAAG (VAN 48 TOKENS) PER DAG, IEDERE TOKEN KAN MAAR 1 KEER GEBRUIKT WORDEN
        requestTokens();

    }

    @FXML
    private void scanQRCode(){
        String dataString = inputDatastring.getText();
        String[] temp = dataString.split(";");
        QRcodeCurrentBar = new QRCode(temp[0],temp[1],temp[2].getBytes());
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
            System.out.println("Dit is de hashBar die we naar de mixing sturen: " + QRcodeCurrentBar.getHashBar());
            Capsule capsule = new Capsule(currentToken, QRcodeCurrentBar.getHashBar());

            boolean doSign = mixingProxyInterface.sendCapsule(capsule);
            if (doSign) {
                //dan moet men de sign ontvangen
                System.out.println("Dit is de hashbar als we hem naar sign sturen: " + capsule.getHashBar());
                byte[] signedCapsule = mixingProxyInterface.signCapsule(capsule);
                System.out.println("Dit is de bytearray van de sign: " + Arrays.toString(signedCapsule));

                //KIJKEN WELK LOGO DIE MOET KRIJGEN
                //BEETJE INT MIDDEN ANDERS STEEDS ZELFDE GETAL
                showLogo(signedCapsule[10]);
            } else {
                System.out.println("bezoek gefailed, waarschijnlijk door een check");
            }
            //GEBRUIKT TOKEN VERWIJDEREN
            tokens.remove(0);
        } else {
            System.out.println("U kan deze bar helaas niet meer bezoeken, uw tokens voor vandaag zijn verbruikt");
        }
    }

    @FXML
    private void verlaatBar() throws RemoteException {
        //LEAVING TIME IN CAPSULE GAAN FIXEN, TERGELIJK NOG KEER DAT OBECT TERUGSTUREN VOOR ONS BEZOEK AAN TE MAKEN
        Capsule currentCapsule = mixingProxyInterface.requestLeaving(currentToken);
        //BEZOEK LOKAAL OPSLAAN
        Bezoek bezoek = new Bezoek(currentCapsule.getTimestampEntered(),currentCapsule.getTimestampLeaving(),QRcodeCurrentBar.getRandomGetal(),QRcodeCurrentBar.getBusinessNumber(),QRcodeCurrentBar.getHashBar());
        bezoeken.add(bezoek);   //hoeft niet mer perse
        sendToLocalDatabase(bezoek);
    }

    private void requestTokens() throws RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        tokens = registrarInterface.requestDailyCustomerToken(phonenumber);
    }

    private void showLogo(int signedCapsule) {
        int eersteGetal = Math.abs(signedCapsule);
        for(String currentInterval: mappingIcons.keySet()){
            String[] interval = currentInterval.split("-");
            if ((Integer.parseInt(interval[0])<= eersteGetal) && (eersteGetal<= Integer.parseInt(interval[1]))){
                System.out.println("Het getal bevindt zich in het interval " + currentInterval + " en krijgt de " + mappingIcons.get(currentInterval) + " toegewezen");
                String path = "src/Resources/Icons/" + mappingIcons.get(currentInterval);
                File file = new File(path);
                try {
                    Image image = new Image(new FileInputStream(file));
                    imageViewSign.setImage(image);
                } catch (FileNotFoundException e) {
                    System.out.println("Image niet gevonden in de resource folder");
                }
                break;
            } else if (100 <= eersteGetal){
                System.out.println("Het getal is groter dan 100, en krijgt de default waarde mee");
                File file = new File("src/Resources/Icons/Thunder.jpg");
                try {
                    Image image = new Image(new FileInputStream(file));
                    imageViewSign.setImage(image);
                } catch (FileNotFoundException e) {
                    System.out.println("Image niet gevonden in de resource folder");
                }
                break;
            } else if (0 > eersteGetal){
                System.out.println("Er is iets misgelopen met uw ABS");
            }
        }
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
                System.out.println("File bestaat nog niet");
                bw.append("Timestamp entering;Timestamp leaving;Random number bar;Bussiness number bar;Hash bar");
                bw.newLine();
                bw.append(bezoek.getTimestampEntered() + ";" + bezoek.getTimestampLeaving()+ ";" + bezoek.getRandomIntBar()+ ";" + bezoek.getBusinessNumberBar()+ ";" + bezoek.getHashBar());
                bw.newLine();
            } else {
                System.out.println("File bestaat wel al");
                bw.append(bezoek.getTimestampEntered() + ";" + bezoek.getTimestampLeaving()+ ";" + bezoek.getRandomIntBar()+ ";" + bezoek.getBusinessNumberBar()+ ";" + bezoek.getHashBar());
                bw.newLine();
            }
            sc.close();
            bw.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }


}
