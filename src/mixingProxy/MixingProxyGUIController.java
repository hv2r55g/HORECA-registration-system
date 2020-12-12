package mixingProxy;

import customer.Bezoek;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import matchingService.MatchingServiceInterface;
import registrar.RegistrarInterface;
import registrar.Token;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.rmi.RemoteException;


public class MixingProxyGUIController extends UnicastRemoteObject  implements MixingProxyInterface, Remote {
    @FXML
    TableView tableViewCapsules;

    @FXML
    Button buttonFlush;

    private ObservableList<Capsule> capsules;
    private PublicKey publicKeyToday;
    private KeyPair keyPairOfTheDay;
    private String dagVanVandaag;
    private Map<Character,String> mappingIcons;
    private RegistrarInterface registrarInterface;
    private MatchingServiceInterface matchingServiceInterface;

    public MixingProxyGUIController() throws RemoteException { super(); }

    public void initController() throws RemoteException {
        initConnecties();
        initAttributen();
        initLogos();
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

        this.mappingIcons = new HashMap<>();
    }

    private void initLogos(){
        //1 KEER PER DAG BEIDE LIJSTEN SHUFFELEN? KWEET ALLEEN NIET GOED HOE IK HET MOET IMPLEMENTEREN
        //VOORLOPIG GEWOON INDEXEN GELIJK STELLEN AAN ELKAAR
        char[] alfabet = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p','q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
        String[] afbeeldingen = {"BlueDot.jpg","BlueSquare.jpg","BlueStar.jpg","BlueTriangle.jpg","BlueHeart.jpg","BlueRuit.jpg",
                "YellowDot.jpg","YellowSquare.jpg","YellowStar.jpg","YellowTriangle.jpg", "YellowHeart.jpg","YellowRuit.jpg",
                "GreenDot.jpg","GreenSquare.jpg","GreenStar.jpg","GreenTriangle.jpg","GreenHeart.jpg","GreenRuit.jpg",
                "RedDot.jpg","RedSquare.jpg","RedStar.jpg","RedTriangle.jpg","RedHeart.jpg","RedRuit.jpg",
                "PinkRuit.jpg","OrangeRuit.jpg",};
        //SHUFFELEN
        List<Character> alfabetArray = new ArrayList<>();
        for (Character c: alfabet){
            alfabetArray.add(c);
        }
        List<String> afbeeldingenArray = Arrays.asList(afbeeldingen);
        Collections.shuffle(alfabetArray);
        Collections.shuffle(afbeeldingenArray);

        //boolean evenGroot = alfabet.length == afbeeldingen.length;
        //System.out.println(alfabet.length);
        //System.out.println(afbeeldingen.length);
        //System.out.println("Zijn de sizes even groot? " + evenGroot);
        if (alfabet.length == afbeeldingen.length){
            for (int i = 0; i < alfabet.length; i++) {
                mappingIcons.put(alfabetArray.get(i),afbeeldingenArray.get(i));
            }
        }
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
            Naming.rebind("rmi://" + hostname + "/" + servicename, this);
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
        boolean b = signatureVerify.verify(sign);
        //System.out.println(b);
        return b;
    }

    public boolean checkDayOfToken(Capsule capsule) throws RemoteException, NoSuchAlgorithmException {
        //OMZETTEN NAAR BYTEaRRAYS
        byte[] data = Base64.getDecoder().decode(capsule.getTokenCustomer().getDatumInfo().getBytes());
        byte[] dataBytes = Arrays.copyOfRange(data,20,data.length);

        String dateValue = new String(dataBytes);
        //System.out.println("Dit zou normaal de value moeten zijn van den datem die van in de sign steken " + dateValue);
        //System.out.println("Dit is de dag van vandaag: " + dagVanVandaag);

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
        //System.out.println("Is het een nieuwe token? " + isNewToken);
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
        //STRING GAAN SIGNEN EN DAN GEPASTE AFBEELDING GAAN DOORSTUREN
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(keyPairOfTheDay.getPrivate());
        //System.out.println("Voor: "+capsule.getHashBar());
        byte[] result = Base64.getDecoder().decode(capsule.getHashBar());
        signature.update(result);
        //System.out.println("Na: "+capsule.getHashBar());
        byte[] ACK = signature.sign();
        String ACKString = Base64.getEncoder().encodeToString(ACK);
        System.out.println("De signed hash ziet er zo uit: "+ACKString);

        //NU AFBEELDING GAAN ZOEKEN IN DE MAPPING
        String titelIcon = zoekAfbeelding(ACKString);
        return titelIcon;
    }

    @Override
    public String requestLogoOfTheDay(String hashBar) throws RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        //JUIST DEZELFDE METHODE ALS HIERBOVEN, MAAR WOU ZE NIET SAMENVOEGEN VOOR DE DUIDELIJKHEID, DEZE METHODE WORDT
        //OPGEROEPEN ALS DE BAR OPEN GAAT

        //STRING GAAN SIGNEN EN DAN GEPASTE AFBEELDING GAAN DOORSTUREN
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(keyPairOfTheDay.getPrivate());
        byte[] result = Base64.getDecoder().decode(hashBar);
        signature.update(result);
        byte[] ACK = signature.sign();
        String ACKString = Base64.getEncoder().encodeToString(ACK);

        //NU AFBEELDING GAAN ZOEKEN IN DE MAPPING
        String titelIcon = zoekAfbeelding(ACKString);
        return titelIcon;
    }

    private String zoekAfbeelding(String ackString) {
        //10DE CHAR VOOR BEETJE WILLEKEUR
        char karakterKey = ackString.charAt(10);
        //System.out.println("Dit is het karakter: " + Character.toLowerCase(karakterKey));
        if (mappingIcons.containsKey(Character.toLowerCase(karakterKey))) {
            //CHAR BEVINDT ZICH IN DE STRING
            return mappingIcons.get(Character.toLowerCase(karakterKey));
        } else {
            //CHAR ZAL EEN SYMBOOL ZIJN
            return "Thunder.jpg";
        }
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

    @Override
    public void sendACK(List<Capsule> ACKs) throws RemoteException {
        capsules.addAll(ACKs);
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
        System.out.println("Zoveel capsules naar matching: " + toMatchingCapsules.size());
        Collections.shuffle(toMatchingCapsules);
        matchingServiceInterface.addCapsules(toMatchingCapsules);

    }

    //------------------------------------------------------------------------------------------------------------------------------------------//

}
