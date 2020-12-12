package matchingService;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import customer.Bezoek;
import doctor.CriticalTuple;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import mixingProxy.Capsule;
import registrar.RegistrarInterface;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class MatchingServiceGUIController extends UnicastRemoteObject implements  MatchingServiceInterface,Remote {
    @FXML
    Button buttonUninformed;

    @FXML
    Button buttonPrintInfected;

    @FXML
    TableView tableViewCapsules;

    @FXML
    TableView tableViewLogs;

    private RegistrarInterface registrarInterface;
    private ObservableList<Capsule> capsulesDB;
    private ObservableList<Bezoek> bezoekenLogs;
    private List<Capsule> infectedCapsules = new ArrayList<>();
    private ObservableList<CriticalTuple> criticalTuples;
    private ListMultimap<String, String> mappingDayNyms = ArrayListMultimap.create();
    private List<String> infectedCF;

    public MatchingServiceGUIController() throws RemoteException {
        super();
    }

    public void initController() {
        initAttributen();
        initConnecties();
        initTables();

    }

    private void initTables() {
        TableColumn columnTimeEntered = new TableColumn("Time entered");
        columnTimeEntered.setMinWidth(100);
        columnTimeEntered.setCellValueFactory(new PropertyValueFactory<Capsule,String>("timestampEnteredString"));
        columnTimeEntered.prefWidthProperty().bind(tableViewCapsules.widthProperty().multiply(1.0/6.0));

        TableColumn columnTimeLeaving = new TableColumn("Time left");
        columnTimeLeaving.setMinWidth(100);
        columnTimeLeaving.setCellValueFactory(new PropertyValueFactory<Capsule,String>("timestampLeavingString"));
        columnTimeLeaving.prefWidthProperty().bind(tableViewCapsules.widthProperty().multiply(1.0/6.0));

        TableColumn columnTokenSign = new TableColumn("Token sign");
        columnTokenSign.setMinWidth(100);
        columnTokenSign.setCellValueFactory(new PropertyValueFactory<Capsule,String>("tokenSign"));
        columnTokenSign.prefWidthProperty().bind(tableViewCapsules.widthProperty().multiply(1.0/6.0));

        TableColumn columnTokenData = new TableColumn("Token data");
        columnTokenData.setMinWidth(100);
        columnTokenData.setCellValueFactory(new PropertyValueFactory<Capsule,String>("tokenData"));
        columnTokenData.prefWidthProperty().bind(tableViewCapsules.widthProperty().multiply(1.0/6.0));

        TableColumn columnHashBar = new TableColumn("Hash bar");
        columnHashBar.setMinWidth(200);
        columnHashBar.setCellValueFactory(new PropertyValueFactory<Capsule,String>("hashBar"));
        columnHashBar.prefWidthProperty().bind(tableViewCapsules.widthProperty().multiply(1.0/6.0));

        TableColumn columnGeinformeerd = new TableColumn("Geinformeerd?");
        columnGeinformeerd.setMinWidth(200);
        columnGeinformeerd.setCellValueFactory(new PropertyValueFactory<Capsule,Boolean>("geinformeerd"));
        columnGeinformeerd.prefWidthProperty().bind(tableViewCapsules.widthProperty().multiply(1.0/6.0));

        tableViewCapsules.setItems(capsulesDB);
        tableViewCapsules.getColumns().addAll(columnTimeEntered,columnTimeLeaving,columnTokenSign,columnTokenData,columnHashBar,columnGeinformeerd);

        TableColumn columnTimeEnteredLogs = new TableColumn("Time entered");
        columnTimeEnteredLogs.setMinWidth(200);
        columnTimeEnteredLogs.setCellValueFactory(new PropertyValueFactory<Bezoek,String>("timestampEnteredString"));
        columnTimeEnteredLogs.prefWidthProperty().bind(tableViewLogs.widthProperty().multiply(1.0/6.0));

        TableColumn columnTimeLeavingLogs = new TableColumn("Time Left");
        columnTimeLeavingLogs.setMinWidth(200);
        columnTimeLeavingLogs.setCellValueFactory(new PropertyValueFactory<Bezoek,String>("timestampLeavingString"));
        columnTimeLeavingLogs.prefWidthProperty().bind(tableViewLogs.widthProperty().multiply(1.0/6.0));

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

        TableColumn columnHashBarLogs = new TableColumn("Hash bar");
        columnHashBarLogs.setMinWidth(200);
        columnHashBarLogs.setCellValueFactory(new PropertyValueFactory<Bezoek,String>("hashBar"));
        columnHashBarLogs.prefWidthProperty().bind(tableViewLogs.widthProperty().multiply(1.0/6.0));

        tableViewLogs.setItems(bezoekenLogs);
        tableViewLogs.getColumns().addAll(columnTimeEnteredLogs,columnTimeLeavingLogs,columnToken,columnRandomIntBar,columnBusinessNumberBar,columnHashBarLogs);

    }

    private void initConnecties() {
        startRMIRegistry();
        String hostname = "localhost";
        String servicename = "MatchingServiceService";
        String clientService = "RegistrarListening";
        String servicenameReg = "RegistrarService";

        try {
            //REMI SERVER OPZETTEN
            Naming.rebind("rmi://" + hostname + "/" + servicename, this);
            System.out.println("RMI Server Matching Service successful started");

            //CONNECTEN MET REGISTRAR
            Naming.rebind("rmi://" + hostname + "/" + clientService, this);
            registrarInterface = (RegistrarInterface) Naming.lookup("rmi://" + hostname + "/" + servicenameReg);

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }

    }

    public static void startRMIRegistry() {
        try {
            java.rmi.registry.LocateRegistry.createRegistry(3099);
            System.out.println("RMI Server Matching Service ready");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void initAttributen() {
        capsulesDB = FXCollections.observableArrayList();
        criticalTuples = FXCollections.observableArrayList();
        bezoekenLogs = FXCollections.observableArrayList();
        infectedCF = new ArrayList<>();
    }

    @Override
    public void addCapsules(List<Capsule> nieuweCapsules) throws RemoteException {
        boolean matchGevonden = false;
        //HIER NOG CHECKEN OF ER GEEN DUPLICATE IS
        for(Capsule nieuweCapsule: nieuweCapsules){
            for (Capsule currentCapsule: capsulesDB){
                if (currentCapsule.getTokenCustomer().getSignature().equals(nieuweCapsule.getTokenCustomer().getSignature())){
                    //System.out.println("Dit is de huidige capsule voor: " + currentCapsule);
                    currentCapsule.setGeinformeerd(true);
                    matchGevonden = true;
                    //System.out.println("Dit is de huidige capsule na: " + currentCapsule);
                    tableViewCapsules.refresh();
                }
            }
            if (!matchGevonden){
                capsulesDB.add(nieuweCapsule);
            }
        }
    }

    @Override
    public void receiveInfectedBezoeken(List<Bezoek> infectedBezoeken) throws IOException, NoSuchAlgorithmException {
        //STAP 1: ALLE CAPSULES DIE OVEREENKOMEN MET DE BEZOEKEN, KORTOM DE USER ZIJN EIGEN CAPSULES GAAN ZOEKEN, EN DE INFORMED TAG OP TRUE ZETTEN --> DE OVERIGE WORDEN OP FALSE GEZET OF BLIJVEN STAAN
        // DEZE ACTIE STAAT DUS GEWOON GELIJK AAN HET AL OP GEINFROMEERD ZETTEN VAN DE USER ZIJN EIGEN TOKENS
        //STAP 2: AANMAKEN VAN EEN CRITICAL TUPLE, DEZE TUPELS GAAN OPSLAAN TOT EEN USER ZE OPVRAAGD
        bezoekenLogs.clear();
        bezoekenLogs.addAll(infectedBezoeken);
        System.out.println("De patient bracht zoveel bezoeken "+bezoekenLogs.size());
        setInformed(bezoekenLogs);

        //WELKE CAFES WAREN INFECTED?
        int incubatietijd = 7;
        mappingDayNyms = registrarInterface.getMappingDayNyms(incubatietijd);
        SimpleDateFormat df = new SimpleDateFormat("ddMMMMyyyy");
        for (Bezoek currentBezoek: infectedBezoeken){
            String currentDatum = df.format(currentBezoek.getCapsule().getTimestampEntered());
            byte[] ri = Base64.getDecoder().decode(currentBezoek.getRandomIntBar());
            for (String currentNym: mappingDayNyms.get(currentDatum)){
                byte[] currentNymByteArray = Base64.getDecoder().decode(currentNym);
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);

                //BYTE ARRAY MAKEN --> RANDOM EN NYM
                dos.write(ri);
                //NYM MOET TERUG GeDECODED WORDEN
                dos.write(currentNymByteArray);
                dos.flush();

                //GAAN HASHEN
                byte[] nieuweHash = messageDigest.digest(bos.toByteArray());
                String nieuweHashString = Base64.getEncoder().encodeToString(nieuweHash);

                dos.close();
                bos.close();

                //System.out.println("hash van de bezoeker:\t "+currentBezoek.getHashBar());
                //System.out.println("hash nieuw gemaakt:\t "+ nieuweHashString);

                if (currentBezoek.getHashBar().equals(nieuweHashString)){
                    System.out.println("We hebben een match= " + currentNym);
                    infectedCF.add(currentNym);
                }

            }
        }

        //STAP 3: MATCHING IS KLAAR MET ZIJN WERK EN WACHT TOT USER EEN GETCRITICALTUPLE REQUEST DOEN, ZIE IN HIERONDER
    }

    @Override
    public List<CriticalTuple> requestCriticalTuples() throws RemoteException {
        List<CriticalTuple> temp = new ArrayList<>();
        for (CriticalTuple t: criticalTuples){
            temp.add(t);
        }
        return temp;
    }

    public void setInformed(List<Bezoek> bezoekenPatient) throws RemoteException {
        for (Bezoek bezoek : bezoekenPatient){
            for (Capsule capsule : capsulesDB){
                if (capsule.getTokenCustomer().getSignature().equals(bezoek.getCapsule().getTokenCustomer().getSignature())){
                    capsule.setGeinformeerd(true);
                    criticalTuples.add(new CriticalTuple(capsule.getHashBar(),capsule.getTimestampEntered(),capsule.getTimestampLeaving()));
                    //System.out.println("Dit was de capsule: " + capsule);
                    System.out.println("Nieuwe tuple toegevoegd: " + criticalTuples.get(0));
                    tableViewCapsules.refresh();
                }
            }
        }

    }

    @FXML
    public void sendUninformedCapsules() throws RemoteException {
        List<Capsule> uninformedCapsules = new ArrayList<>();
        List<Capsule> neededCapsules = new ArrayList<>();

        for (Capsule capsule : capsulesDB){
            if (capsule.isGeinformeerd()==false){
                uninformedCapsules.add(capsule);
            }
        }

        for (Capsule capsule : uninformedCapsules){
            for (CriticalTuple criticalTuple : criticalTuples){
                if (capsule.getHashBar().equals(criticalTuple.getHashBar()) && capsule.isErOverlap(criticalTuple)){
                    neededCapsules.add(capsule);
                }
            }
        }

        //NU NOG DEZE UNINFORMED NAAR REGISTRAR
        registrarInterface.sendUninformedCustomers(neededCapsules);
    }

    @FXML
    public void printInfectedCF(){
        System.out.println("Dit waren alle infected CFs:");
        for (String s: infectedCF){
            System.out.println(s);
        }
    }
}
