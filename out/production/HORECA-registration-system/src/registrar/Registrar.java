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
import java.security.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class Registrar implements RegistrarInterface {
    private int aantalTokensPerCustomer;
    private String dagVanVandaag;
    //private Map<String,List> mappingHashBars;   //Key: Datum; Values: List van nyms die gemaakt zijn die dag
    private ListMultimap<String, String> mappingDayNyms = ArrayListMultimap.create();
    private Map<String,List> mappingTokens;
    private KeyPair keyPairOfTheDay;
    private SecretKey masterKey;

    public int getAantalTokensPerCustomer() {
        return aantalTokensPerCustomer;
    }

    public Map<String, List> getMappingTokens() {
        return mappingTokens;
    }

    public PrivateKey getPrivateKeyOfTheDay(){
        return keyPairOfTheDay.getPrivate();
    }

    public SecretKey getMasterKey() {
        return masterKey;
    }

    public Registrar() throws NoSuchAlgorithmException {
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

    private String createHash(SecretKeySpec currentKey, int bussinesNumber, String day) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        //BRON: https://www.novixys.com/blog/hmac-sha256-message-authentication-mac-java/
        //BRON: https://examples.javacodegeeks.com/core-java/crypto/generate-message-authentication-code-mac/

        //DE HASHFUNCTIE IS GEBASEERD OP DE DAILY KEY
        //TODO: uitleggen waarom we voor deze gekozen hebben sws op de presentatie
        String algoritme = "HMACSHA1";
        Mac mac = Mac.getInstance(algoritme);
        mac.init(currentKey);
        //TE HASHEN DATA
        String encodedKey = Base64.getEncoder().encodeToString(currentKey.getEncoded());
        //System.out.println("Zo ziet de encoded key eruit: " + encodedKey);
        String teHashenInfo = encodedKey + bussinesNumber + day;
        byte[] teHashenInfoInBytes = teHashenInfo.getBytes("UTF-8");
        byte[] result = mac.doFinal(teHashenInfoInBytes);
        String resultString = new String(result, StandardCharsets.UTF_8);
        //System.out.println("Hoe ziet zo'n hash eruit: " + resultString);
        //TODO: nog otevoegen aan de mapping
        return resultString;
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
    public PublicKey getPublicKeyOfTheDay() {
        return keyPairOfTheDay.getPublic();
    }

    @Override
    public PrivateKey getPrivatekeyOftheDay() throws RemoteException {
        return keyPairOfTheDay.getPrivate();
    }

    @Override
    public String getDagVanVandaag() throws RemoteException {
        return dagVanVandaag;
    }

    @Override
    public ListMultimap<String, String> getMappingDayNyms(int incubatieTijd) throws RemoteException {
        ListMultimap<String,String> result = ArrayListMultimap.create();

        //ALLE NYMS TERUGGEVEN DIE INCUBATIETIJD TERUG GAAN
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("ddMMMMyyyy");

        //HUIDIGE DAG TOEVOEGEN
        result.putAll(dagVanVandaag,mappingDayNyms.get(dagVanVandaag));

        for (int i = 0; i < incubatieTijd-1; i++) { //Min 1 omdat de huidige dag hierboven al gedaan wordt
            //DE EERSTE ZULLEN GISTEREN ZIJN, DAN EERGISTEREN? ENZOVOORT
            calendar.add(Calendar.DAY_OF_YEAR,-1);
            String dag = df.format(calendar.getTime());
            result.putAll(dag,mappingDayNyms.get(dag));
        }

        //TER CONTROLE KEER ALLE KEYS UITPRINTEN
        System.out.println("Alle keys van de incubatietijd");
        for (String s : result.keySet()) {
            System.out.println("-------------------------------------------------------------------------");
            System.out.println("Key: " + s);
            for (String hash: result.get(s)){
                System.out.println("Value: " + hash);
            }
        }

        return result;
    }

    @Override
    public List<String> requestMonthlyHash(int bussinesNumber) throws RemoteException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        System.out.println("Create mothly keys for: " + bussinesNumber);
        int aantalDagen = 10;
        List<String> monthlyHash = new ArrayList<>();

        Date date = new Date();
        SimpleDateFormat df  = new SimpleDateFormat("ddMMMMyyyy");
        Calendar c1 = Calendar.getInstance();

        c1.add(Calendar.DAY_OF_YEAR,-10);   //Efkes terugkeren in de tijd om dingen te testen met incubatietijd

        for (int i = 0; i < aantalDagen; i++) {
            SecretKeySpec key;
            String hash;
            if (i == 0){
                String currentDate = df.format(date);
                //System.out.println(currentDate);
                key = generateDailyKey(bussinesNumber, currentDate);
                hash = createHash(key,bussinesNumber,currentDate);
                mappingDayNyms.put(currentDate,hash);
            } else {
                //1 DAG AAN DE CALENDAR TOEVOEGEN
                c1.add(Calendar.DAY_OF_YEAR, 1);
                Date nextDate = c1.getTime();
                String dueDate = df.format(nextDate);
                //System.out.println(dueDate);
                key = generateDailyKey(bussinesNumber, dueDate);
                hash = createHash(key,bussinesNumber,dueDate);
                mappingDayNyms.put(dueDate,hash);
            }
            //NIET VERGETEN TOE TE VOEGEN AAN DE ARRAY
            monthlyHash.add(hash);
        }
        return monthlyHash;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------//
}
