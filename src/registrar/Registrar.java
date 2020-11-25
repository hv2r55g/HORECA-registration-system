package registrar;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Registrar implements RegistrarInterface {

    private SecretKey masterKey;

    public Registrar() throws NoSuchAlgorithmException {
        super();
        createMasterKey();
    }

    public SecretKey getMasterKey() {
        return masterKey;
    }

    public void setMasterKey(SecretKey masterKey) {
        this.masterKey = masterKey;
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        startRMIRegistry();
        String hostname = "localhost";
        String servicename = "RegistrarService";
        try {
            Registrar obj = new Registrar();
            RegistrarInterface stub = (RegistrarInterface) UnicastRemoteObject.exportObject(obj, 0);
            Naming.rebind("rmi://" + hostname + "/" + servicename, stub);
            System.out.println("RMI Server successful started");
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Server failed starting ...");
        }
    }

    public static void startRMIRegistry() {
        try {
            java.rmi.registry.LocateRegistry.createRegistry(1099);
            System.out.println("RMI Server ready");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    //-------------------------------------------------------OVERIGE METHODES-------------------------------------------------------------------//

    public void createMasterKey() throws NoSuchAlgorithmException {
        //UNIEKE MASTERKEY VAN DE REGISTRAR GAAN GENEREREN, PER NIEUWE REGISTRAR DUS OOK NIEUWE MASTERKEY
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        SecretKey aesKey = keygen.generateKey();
        masterKey = aesKey;
    }

    public SecretKeySpec generateDailyKey(int businessNumber, String datum) throws RemoteException {
        String keyData = masterKey.toString()+businessNumber+datum;
        byte[] aesKeyData = keyData.getBytes();
        return new SecretKeySpec(aesKeyData, "AES");
    }

    private byte[] createHash(SecretKeySpec currentKey, int bussinesNumber, String day) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        //BRON: https://www.novixys.com/blog/hmac-sha256-message-authentication-mac-java/
        //BRON: https://examples.javacodegeeks.com/core-java/crypto/generate-message-authentication-code-mac/

        //DE HASHFUNCTIE IS GEBASEERD OP DE DAILY KEY
        //TODO: uitleggen waarom we voor deze gekozen hebben sws op de presentatie
        String algoritme = "HMACSHA1";
        Mac mac = Mac.getInstance(algoritme);
        mac.init(currentKey);
        //TE HASHEN DATA
        String encodedKey = Base64.getEncoder().encodeToString(currentKey.getEncoded());
        System.out.println("Zo ziet de encoded key eruit: " + encodedKey);
        String teHashenInfo = encodedKey + bussinesNumber + day;
        byte[] teHashenInfoInBytes = teHashenInfo.getBytes("UTF-8");
        byte[] result = mac.doFinal(teHashenInfoInBytes);
        String resultString = new String(result, StandardCharsets.UTF_8);
        System.out.println("Hoe ziet zo'n hash eruit: " + resultString);
        return result;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------//

    //-----------------------------------------------OVERIDE METHODES VAN DE INTERFACE----------------------------------------------------------//

    @Override
    public void requestDailyCustomerToken(int phoneNumber) throws RemoteException {
        SecretKeySpec[] tokens = new SecretKeySpec[48];
    }

    @Override
    public List<byte[]> requestMonthlyHash(int bussinesNumber) throws RemoteException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        int aantalDagen = 5;
        List<byte[]> monthlyHash = new ArrayList<>();

        Date date = new Date();
        SimpleDateFormat df  = new SimpleDateFormat("ddMMMMyyyy");
        Calendar c1 = Calendar.getInstance();

        for (int i = 0; i < aantalDagen; i++) {
            SecretKeySpec key;
            byte[] hash;
            if (i == 0){
                String currentDate = df.format(date);
                //System.out.println(currentDate);
                key = generateDailyKey(bussinesNumber, currentDate);
                hash = createHash(key,bussinesNumber,currentDate);
            } else {
                //1 DAG AAN DE CALENDAR TOEVOEGEN
                c1.add(Calendar.DAY_OF_YEAR, 1);
                Date nextDate = c1.getTime();
                String dueDate = df.format(nextDate);
                //System.out.println(dueDate);
                key = generateDailyKey(bussinesNumber, dueDate);
                hash = createHash(key,bussinesNumber,dueDate);
            }
            //NIET VERGETEN TOE TE VOEGEN AAN DE ARRAY
            monthlyHash.add(hash);
        }


        return monthlyHash;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------//
}
