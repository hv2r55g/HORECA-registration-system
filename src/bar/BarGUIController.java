package bar;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import mixingProxy.MixingProxyInterface;
import registrar.RegistrarInterface;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Base64;
import java.util.List;
import java.util.Random;

public class BarGUIController extends UnicastRemoteObject implements Remote {
    @FXML
    TextField textFieldBusinessNr;

    @FXML
    Button buttonOpenCatering;

    @FXML
    Button buttonCreateMothleyHashes;

    @FXML
    ImageView imageViewSign;

    @FXML
    Label labelBusinessNr;

    @FXML
    Label labelRandomInt;

    @FXML
    Label labelHashBar;

    @FXML
    TextField labelPrint;


    private String bussinesNumber;
    private List mothlyHash;
    private QRCode qrCode;
    private RegistrarInterface registrarInterface;
    private MixingProxyInterface mixingProxyInterface;

    public BarGUIController() throws RemoteException {
        super();
    }

    private void initAttributen() {
        textFieldBusinessNr.clear();
    }

    private void initConnecties() throws MalformedURLException, RemoteException, NotBoundException {
        //Connecten met de registrar
        String hostname = "localhost";
        String clientService = "RegistrarListening";
        String servicename = "RegistrarService";
        Naming.rebind("rmi://" + hostname + "/" + clientService, this);
        registrarInterface = (RegistrarInterface) Naming.lookup("rmi://" + hostname + "/" + servicename);

        //Connecten met de Mixing proxy
        clientService = "MixingProxyListening";
        servicename = "MixingProxyService";
        Naming.rebind("rmi://" + hostname + "/" + clientService, this);
        mixingProxyInterface = (MixingProxyInterface) Naming.lookup("rmi://" + hostname + "/" + servicename);

    }

    public void initController() throws RemoteException, NotBoundException, MalformedURLException {
        initAttributen();
        initConnecties();
    }

    @FXML
    private void scanBusinessNr(){
        this.bussinesNumber = textFieldBusinessNr.getText();
        labelBusinessNr.setText(this.bussinesNumber);
        textFieldBusinessNr.clear();
        textFieldBusinessNr.setDisable(true);
    }


    @FXML
    private void requestMonthlyHash() throws RemoteException, NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        //LIJST VAN STRING WORDT TERUG GEGEVEN
        mothlyHash = registrarInterface.requestMonthlyHash(bussinesNumber);
        buttonOpenCatering.setDisable(false);
    }

    @FXML
    private void createQRForToday() throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException, RemoteException, SignatureException {
        //BRON: https://www.novixys.com/blog/hmac-sha256-message-authentication-mac-java/
        //BRON: https://examples.javacodegeeks.com/core-java/crypto/generate-message-authentication-code-mac/

        //HIER OPNIEUW EEN HASHFUNCTIE MAKEN, MAAR GEBASEERD OP ANDERE ZAKEN
        //QR STELLEN WE HIER GEWOON VOOR DOOR STRING VAN DE 3 PARAMETERS

        //DE HASHFUNCTIE IS GEBASEERD OP RANDOM NUMBER
        Random random = new Random();
        int maxGetal = 9999;
        String randomGetal = Integer.toString(random.nextInt(maxGetal));

        //KEY MAKEN WAAROP DE HASHFUNCTIE GEBASEERD IS
        byte[] aesKeyData = randomGetal.getBytes();
        SecretKeySpec keyHashFunction = new SecretKeySpec(aesKeyData, "AES");

        //HASHFUNCTIE INITIALISEREN EN LATEN UITVOEREN
        //TODO: uitleggen waarom we voor deze gekozen hebben sws op de presentatie
        String algoritme = "HMACSHA1";
        Mac mac = Mac.getInstance(algoritme);
        mac.init(keyHashFunction);
        //TE HASHEN DATA
        //TODO: hier naar de demo wss niet zomaar eerste hash nemen
        String teHashenInfo = randomGetal + mothlyHash.get(0);
        byte[] teHashenInfoInBytes = teHashenInfo.getBytes("UTF-8");
        byte[] result = mac.doFinal(teHashenInfoInBytes);
        String QrString = Base64.getEncoder().encodeToString(result);
        //String resultString = new String(result, StandardCharsets.UTF_8);
        //System.out.println("Hoe ziet zo'n hash voor de QR code eruit: " + resultString);

        //NOG DE 3 PARAMETERS OPSLAAN OM IN DE TOEKOMST EEN QR CODE TE MAKEN
        qrCode = new QRCode(randomGetal,bussinesNumber,QrString);
        labelRandomInt.setText(randomGetal);
        labelHashBar.setText(QrString);
        printQR();
        getLogoOfTheDay();

    }

    private void getLogoOfTheDay() throws RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String titelLogo = mixingProxyInterface.requestLogoOfTheDay(qrCode.getHashBar());
        String path = "src/Resources/Icons/" + titelLogo;
        File file = new File(path);
        try {
            Image image = new Image(new FileInputStream(file));
            imageViewSign.setImage(image);
        } catch (FileNotFoundException e) {
            System.out.println("Image niet gevonden in de resource folder");
        }
    }

    public void printQR(){
        labelPrint.setText(qrCode.getRandomGetal()+";"+qrCode.getBusinessNumber()+";"+qrCode.getHashBar()+";");
    }





}
