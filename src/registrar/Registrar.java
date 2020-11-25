package registrar;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Registrar implements RegistrarInterface {
    private int aantalTokensPerCustomer;
    private String dagVanVandaag;
    private Map<String,List> mappingTokens;
    private KeyPair keyPairOfTheDay;
    private SecretKey masterKey;

    public Registrar() throws NoSuchAlgorithmException {
        super();
        createMasterKey();
        aantalTokensPerCustomer = 20;
        mappingTokens = new HashMap<>();
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");
        keyPairOfTheDay = keyPairGenerator.generateKeyPair();
        Date date = new Date();
        SimpleDateFormat df  = new SimpleDateFormat("ddMMMMyyyy");
        dagVanVandaag = df.format(date);
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
        //String resultString = new String(result, StandardCharsets.UTF_8);
        System.out.println("Hoe ziet zo'n hash eruit: " + result);
        return result;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------//

    //-----------------------------------------------OVERIDE METHODES VAN DE INTERFACE----------------------------------------------------------//

    @Override
    public List requestDailyCustomerToken(String phoneNumber) throws RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        System.out.println("Generate tokens for " + phoneNumber);
        List<byte[]> tokens = new ArrayList<>();
        for (int i = 0; i < aantalTokensPerCustomer; i++) {
            Signature signature = Signature.getInstance("SHA256WithDSA");
            SecureRandom secureRandom = new SecureRandom();
            signature.initSign(keyPairOfTheDay.getPrivate(),secureRandom);
            signature.update(dagVanVandaag.getBytes());
            byte[] token = signature.sign();
            tokens.add(token);
        }

        //NOG GAAN MAPPEN DAT WE DEZE TOKENS AAN DAT TELEFOONNUMMER GEGEVEN HEBBEN
        mappingTokens.put(phoneNumber,tokens);

        //AT THE END TOKENS NAAR GEBRUIKER STUREN
        return tokens;
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
