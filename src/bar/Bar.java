package bar;

import registrar.RegistrarInterface;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.net.MalformedURLException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Random;

public class Bar extends UnicastRemoteObject implements Remote {

    private String bussinesNumber;
    private List mothlyHash;
    private QRCode qrCode;
    private RegistrarInterface registrarInterface;

    public Bar() throws RemoteException {}

    public Bar(String bussinesNumber) throws RemoteException {
        this.bussinesNumber = bussinesNumber;
    }

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException, NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        //HANDMATIGE INPUT
        //Scanner sc = new Scanner(System.in);
        //System.out.println("Geef een bussiness number: ");
        //int bussinesNumberFromScanner = sc.nextInt();
        String bussinesNumberFromScanner = Integer.toString(1);

        Bar currentBar = new Bar(bussinesNumberFromScanner);

        //Connecten met de registrar
        String hostname = "localhost";
        String clientService = "RegistrarListening";
        String servicename = "RegistrarService";
        Naming.rebind("rmi://" + hostname + "/" + clientService, currentBar);
        RegistrarInterface registrarInterface = (RegistrarInterface) Naming.lookup("rmi://" + hostname + "/" + servicename);
        currentBar.registrarInterface = registrarInterface;

        //1 KEER PER MAAND DE KEYS OPVRAGEN
        currentBar.requestMonthlyHash();

        //NU KEER SIMULEREN DAT WE DE HUIDIGE DAG WILLEN OPEN DOEN, DUS GEWOON EERSTE HASH UIT LIJST NEMEN
        currentBar.createQRForToday();
        currentBar.printQR();
    }


    //-------------------------------------------------------OVERIGE METHODES-------------------------------------------------------------------//
    private void requestMonthlyHash() throws RemoteException, NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        mothlyHash = registrarInterface.requestMonthlyHash(bussinesNumber);
    }

    private void createQRForToday() throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        //BRON: https://www.novixys.com/blog/hmac-sha256-message-authentication-mac-java/
        //BRON: https://examples.javacodegeeks.com/core-java/crypto/generate-message-authentication-code-mac/

        //HIER OPNIEUW EEN HASHFUNCTIE MAKEN, MAAR GEBASEERD OP ANDERE ZAKEN
        //QR STELLEN WE HIER GEWOON VOOR DOOR STRING VAN DE 3 PARAMETERS

        //DE HASHFUNCTIE IS GEBASEERD OP RANDOM NUMBER
        Random random = new Random();
        int maxGetal = 9999;
        String randomGetal = Integer.toString(random.nextInt(maxGetal));
        System.out.println("Random getal voor vandaag: " + randomGetal);

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
        //String resultString = new String(result, StandardCharsets.UTF_8);
        //System.out.println("Hoe ziet zo'n hash voor de QR code eruit: " + resultString);

        //NOG DE 3 PARAMETERS OPSLAAN OM IN DE TOEKOMST EEN QR CODE TE MAKEN
        qrCode = new QRCode(randomGetal,bussinesNumber,result);

    }

    public void printQR(){
        System.out.println(qrCode.getRandomGetal()+";"+qrCode.getBusinessNumber()+";"+qrCode.getHashBar()+";");
    }
    //------------------------------------------------------------------------------------------------------------------------------------------//

}
