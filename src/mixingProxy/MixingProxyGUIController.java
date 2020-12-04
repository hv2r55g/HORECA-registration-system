package mixingProxy;

import customer.Bezoek;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import matchingService.MatchingServiceInterface;
import registrar.RegistrarInterface;
import registrar.Token;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.rmi.RemoteException;


public class MixingProxyGUIController  implements MixingProxyInterface, Remote {
    @FXML
    TableView tableViewCapsules;

    @FXML
    Button buttonFlush;



    private ObservableList<Capsule> capsules;
    private PublicKey publicKeyToday;
    private KeyPair keyPairOfTheDay;
    private String dagVanVandaag;
    private RegistrarInterface registrarInterface;
    private MatchingServiceInterface matchingServiceInterface;

    public MixingProxyGUIController() { super(); }

    public void initController() throws RemoteException {
        initConnecties();
        initAttributen();
        initTable();
        getPublicKey();
    }

    private void initAttributen() {
        capsules = FXCollections.observableArrayList();
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairOfTheDay = keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        //DAG VAN VANDAAG INSTELLEN
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMMMyyyy");
        dagVanVandaag = sdf.format(date);
    }

    private void initTable() {
        TableColumn columnTimeEntered = new TableColumn("Time entered");
        columnTimeEntered.setMinWidth(100);
        columnTimeEntered.setCellValueFactory(new PropertyValueFactory<Capsule,String>("timestampEnteredString"));
        columnTimeEntered.prefWidthProperty().bind(tableViewCapsules.widthProperty().multiply(0.2));

        TableColumn columnTimeLeaving = new TableColumn("Time left");
        columnTimeLeaving.setMinWidth(100);
        columnTimeLeaving.setCellValueFactory(new PropertyValueFactory<Capsule,String>("timestampLeavingString"));
        columnTimeLeaving.prefWidthProperty().bind(tableViewCapsules.widthProperty().multiply(0.2));

        TableColumn columnTokenSign = new TableColumn("Token sign");
        columnTokenSign.setMinWidth(100);
        columnTokenSign.setCellValueFactory(new PropertyValueFactory<Capsule,String>("tokenSign"));
        columnTokenSign.prefWidthProperty().bind(tableViewCapsules.widthProperty().multiply(0.2));

        TableColumn columnTokenData = new TableColumn("Token data");
        columnTokenData.setMinWidth(100);
        columnTokenData.setCellValueFactory(new PropertyValueFactory<Capsule,String>("tokenData"));
        columnTokenData.prefWidthProperty().bind(tableViewCapsules.widthProperty().multiply(0.2));

        TableColumn columnHashBar = new TableColumn("Hash bar");
        columnHashBar.setMinWidth(200);
        columnHashBar.setCellValueFactory(new PropertyValueFactory<Capsule,String>("hashBar"));
        columnHashBar.prefWidthProperty().bind(tableViewCapsules.widthProperty().multiply(0.2));

        tableViewCapsules.setItems(capsules);
        tableViewCapsules.getColumns().addAll(columnTimeEntered,columnTimeLeaving,columnTokenSign,columnTokenData,columnHashBar);
    }

    private void initConnecties() {
        startRMIRegistry();
        String hostname = "localhost";
        String servicename = "MixingProxyService";
        String clientService = "RegistrarListening";
        String servicenameReg = "RegistrarService";
        String servicenameMatchingServer = "MatchingServiceService";

        try {
            MixingProxyInterface stub = (MixingProxyInterface) UnicastRemoteObject.exportObject(this, 0);
            Naming.rebind("rmi://" + hostname + "/" + servicename, stub);
            System.out.println("RMI Server Mixing Proxy successful started");

            //CONNECTEN MET REGISTRAR
            Naming.rebind("rmi://" + hostname + "/" + clientService, this);
            registrarInterface = (RegistrarInterface) Naming.lookup("rmi://" + hostname + "/" + servicenameReg);


            //CONNECTEN MET MATICHING SERVICE
            clientService = "MatchingServiceListening";
            Naming.rebind("rmi://" + hostname + "/" + clientService, this);
            matchingServiceInterface = (MatchingServiceInterface) Naming.lookup("rmi://" + hostname + "/" + servicenameMatchingServer);

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }


    public static void startRMIRegistry() {
        try {
            java.rmi.registry.LocateRegistry.createRegistry(2099);
            System.out.println("RMI Server Mixing proxy ready");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    //-------------------------------------------------------OVERIGE METHODES-------------------------------------------------------------------//


    public boolean checkValidityToken(Capsule capsule) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidKeySpecException {
        Signature signatureVerify = Signature.getInstance("SHA256WithDSA");
        byte[] sign = Base64.getDecoder().decode(capsule.getTokenCustomer().getSignature().getBytes());
        byte[] data = Base64.getDecoder().decode(capsule.getTokenCustomer().getDatumInfo().getBytes());
        signatureVerify.initVerify(publicKeyToday);
        signatureVerify.update(data);
        return signatureVerify.verify(sign);
    }

    public boolean checkDayOfToken(Capsule capsule) throws RemoteException, NoSuchAlgorithmException {
        //OMZETTEN NAAR BYTEaRRAYS
        byte[] data = Base64.getDecoder().decode(capsule.getTokenCustomer().getDatumInfo().getBytes());
        byte[] dataBytes = Arrays.copyOfRange(data,20,data.length);

        String dateValue = new String(dataBytes);
        System.out.println("Dit zou normaal de value moeten zijn van den datem die van in de sign steken " + dateValue);
        System.out.println("Dit is de dag van vandaag: " + dagVanVandaag);

        if (dateValue.equals(dagVanVandaag)){
            return true;
        } else {
            System.out.println("De dag van de token komt niet overeen met de dag van vandaag");
            return false;
        }

    }

    public boolean checkTokenNotSpendYet(Capsule capsule) {
        boolean isNewToken = true;

        if (capsules.isEmpty()) {
            isNewToken = true;
        } else {
            for (Capsule capsuleInLijst : capsules) {
                //TODO: vergelijken van
                if (capsule.getTokenCustomer().getSignature().equals(capsuleInLijst.getTokenCustomer().getSignature())) {
                    isNewToken = false;
                    break;
                }
            }
        }
        System.out.println("Is het een nieuwe token? " + isNewToken);
        return isNewToken;
    }

    public void getPublicKey() throws RemoteException {
        publicKeyToday = registrarInterface.getPublicKeyOfTheDay();
    }

    //------------------------------------------------------------------------------------------------------------------------------------------//


    //-----------------------------------------------OVERIDE METHODES VAN DE INTERFACE----------------------------------------------------------//

    @Override
    public synchronized boolean sendCapsule(Capsule capsule) throws RemoteException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        //TIJDEN VAN DE CAPSULES GAAN TOEVOEGEN
        capsule.setDagBezoek(dagVanVandaag);
        capsule.setTimestampEntered(System.currentTimeMillis());

        if (checkDayOfToken(capsule) && checkTokenNotSpendYet(capsule) && checkValidityToken(capsule)) {
            System.out.println("All checks oke");
            capsules.add(capsule);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String signCapsule(Capsule capsule) throws NoSuchAlgorithmException, SignatureException, RemoteException, InvalidKeyException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(keyPairOfTheDay.getPrivate());
        //System.out.println("Voor: "+capsule.getHashBar());
        byte[] result = Base64.getDecoder().decode(capsule.getHashBar());
        signature.update(result);
        //System.out.println("Na: "+capsule.getHashBar());
        byte[] ACK = signature.sign();
        String ACKString = Base64.getEncoder().encodeToString(ACK);
        System.out.println("De signed hash ziet er zo uit: "+ACKString);
        return ACKString;
    }

    @Override
    public Capsule requestLeaving(Token currentToken) throws RemoteException {
        //TOKEN GAAN ZOEKEN EN EIND TIMESTAMP AAN TOEVOEGEN
        for (Capsule c: capsules){
            if (currentToken.getSignature().equals(c.getTokenCustomer().getSignature())){
                //System.out.println("Capsule voor: " + c.toString());
                c.setTimestampLeaving(System.currentTimeMillis());
                tableViewCapsules.refresh();
                //System.out.println("Capsule na: " + c.toString());
                //NORMAAL ZAL TOKEN UNIEK ZIJN EN MAG DE FORLOOP STOPPEN
                return c;
            }
        }

        //NORMAAL ZAL HIJ ALTIJD CAPSULE VINDEN
        return null;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------//

    //-----------------------------------------------FXMl---------------------------------------------------------------------------------------//
    @FXML
    public void sendCapsulesToMatchingService() throws RemoteException, ParseException {
        System.out.println("Flushen van de capsules");
        //CAPSULES MOETEN EERST GESHUFFELD WORDEN
        List<Capsule> toMatchingCapsules = new ArrayList<>();

        //NA 23U55 AANGEPASTE REGELING
        Date date = new Date() ;
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm") ;
        dateFormat.format(date);
        String sluitingsuur = "23:55";

        if (dateFormat.parse(dateFormat.format(date)).before(dateFormat.parse(sluitingsuur))){
            int aantalCapsulesOorspronkelijk = capsules.size();
            for (int i = aantalCapsulesOorspronkelijk-1; i >= 0; i--) {
                Capsule currentCapsule = capsules.get(i);
                if (currentCapsule.getTimestampLeaving()>=0){
                    //CAPSULE NAAR MATCHING EN VERWIJDEREN UIT MIXING
                    toMatchingCapsules.add(currentCapsule);
                    capsules.remove(currentCapsule);
                }
            }
        } else {
            //NA 23U55
            int aantalCapsulesOorspronkelijk = capsules.size();
            for (int i = aantalCapsulesOorspronkelijk-1; i>=0; i--) {
                Capsule currentCapsule = capsules.get(i);
                if (currentCapsule.getTimestampLeaving()==-1){
                    currentCapsule.setTimestampLeaving(System.currentTimeMillis()); //Ofwel het sluitingsuur
                }
                //OVERIGE CAPSULES NAAR MATCHING EN VERWIJDEREN UIT MIXING
                toMatchingCapsules.add(currentCapsule);
                capsules.remove(currentCapsule);
            }
            //TO BE SURE
            System.out.println("Zijn er nog capsules aanwezig na 23u55?" + capsules.size());
            capsules.clear();
        }

        System.out.println("Nog zoveel capsules aanwezig: " + capsules.size());
        System.out.println("Zoveel capsules naar matiching: " + toMatchingCapsules.size());
        Collections.shuffle(toMatchingCapsules);
        matchingServiceInterface.addCapsules(toMatchingCapsules);

    }

    //------------------------------------------------------------------------------------------------------------------------------------------//

}
