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
import java.io.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
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
    private List<String> mothlyNyms;
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
    private void requestMonthlyHash() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        //LIJST VAN STRING WORDT TERUG GEGEVEN
        mothlyNyms = registrarInterface.requestMonthlyNyms(bussinesNumber);
        System.out.println("HEt aantal nyms die men krijgt: " + mothlyNyms.size());
        buttonOpenCatering.setDisable(false);
    }

    @FXML
    private void createQRForToday() throws NoSuchAlgorithmException, InvalidKeyException, IOException, SignatureException {
        //HIER OPNIEUW EEN HASHFUNCTIE MAKEN, MAAR GEBASEERD OP ANDERE ZAKEN
        //QR STELLEN WE HIER GEWOON VOOR DOOR STRING VAN DE 3 PARAMETERS

        //DE HASHFUNCTIE IS GEBASEERD OP RANDOM NUMBER
        //Random random = new Random();
        //int maxGetal = 9999;
        //String randomGetal = Integer.toString(random.nextInt(maxGetal));
        SecureRandom random = new SecureRandom();
        byte[] randomBytes = new byte[20];
        random.nextBytes(randomBytes);
        String randomGetal = Base64.getEncoder().encodeToString(randomBytes);
        System.out.println("Dit is het random getal: " + randomGetal);

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        //BYTE ARRAY MAKEN --> RANDOM EN NYM
        dos.write(randomBytes);
        //NYM MOET TERUG GeDECODED WORDEN
        byte[] nym = Base64.getDecoder().decode(mothlyNyms.get(0));
        dos.write(nym);
        dos.flush();

        //GAAN HASHEN
        byte[] hashBar = messageDigest.digest(bos.toByteArray());
        String hashBarString = Base64.getEncoder().encodeToString(hashBar);
        System.out.println("Dit is de hash: " + hashBarString);

        dos.close();
        bos.close();

        //KEY MAKEN WAAROP DE HASHFUNCTIE GEBASEERD IS
        //byte[] aesKeyData = randomGetal.getBytes();
        //SecretKeySpec keyHashFunction = new SecretKeySpec(aesKeyData, "AES");

        //HASHFUNCTIE INITIALISEREN EN LATEN UITVOEREN
        //String algoritme = "HMACSHA1";
        //Mac mac = Mac.getInstance(algoritme);
        //mac.init(keyHashFunction);
        //TE HASHEN DATA
        //String teHashenInfo = randomGetal + mothlyNyms.get(0);
        //byte[] teHashenInfoInBytes = teHashenInfo.getBytes("UTF-8");
        //byte[] result = mac.doFinal(teHashenInfoInBytes);
        //String QrString = Base64.getEncoder().encodeToString(result);
        //String resultString = new String(result, StandardCharsets.UTF_8);
        //System.out.println("Hoe ziet zo'n hash voor de QR code eruit: " + resultString);

        //NOG DE 3 PARAMETERS OPSLAAN OM IN DE TOEKOMST EEN QR CODE TE MAKEN
        qrCode = new QRCode(randomGetal,bussinesNumber,hashBarString);
        labelRandomInt.setText(randomGetal);
        labelHashBar.setText(hashBarString);
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
