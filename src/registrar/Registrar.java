package registrar;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
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

    public SecretKeySpec generateDailyKey(String businessNumber, String datum) throws RemoteException {
        String keyData = masterKey.toString()+businessNumber+datum;
        byte[] aesKeyData = keyData.getBytes();
        return new SecretKeySpec(aesKeyData, "AES");
    }

    private String createNym(SecretKeySpec currentKey, String bussinesNumber, String day) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        dos.write(currentKey.getEncoded());
        dos.write(bussinesNumber.getBytes());
        dos.write(day.getBytes());
        dos.flush();

        //GAAN HASHEN
        byte[] nym = messageDigest.digest(bos.toByteArray());
        dos.close();
        bos.close();

        //NOG NAAR STRING OVERZETTEN
        return Base64.getEncoder().encodeToString(nym);


        //DE HASHFUNCTIE IS GEBASEERD OP DE DAILY KEY
        //TODO: Probleem was dat de hash niet geinversed engineerd kan worden
        //String algoritme = "HMACSHA1";
        //Mac mac = Mac.getInstance(algoritme);
        //mac.init(currentKey);
        //TE HASHEN DATA
        //String encodedKey = Base64.getEncoder().encodeToString(currentKey.getEncoded());
        //System.out.println("Zo ziet de encoded key eruit: " + encodedKey);
        //String teHashenInfo = encodedKey + bussinesNumber + day;
        //byte[] teHashenInfoInBytes = teHashenInfo.getBytes("UTF-8");
        //byte[] result = mac.doFinal(teHashenInfoInBytes);
        //String resultString = Base64.getEncoder().encodeToString(result);
        //return resultString;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------//

    //-----------------------------------------------OVERIDE METHODES VAN DE INTERFACE----------------------------------------------------------//

    @Override
    public List requestDailyCustomerToken(String phoneNumber) throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        System.out.println("Generate tokens for " + phoneNumber);
        List<Token> tokens = new ArrayList<>();
        for (int i = 0; i < aantalTokensPerCustomer; i++) {
            //VASTE LENGTE GEVEN AAN DE TOKENS
            SecureRandom random = new SecureRandom();
            byte[] bytes = new byte[20];
            random.nextBytes(bytes);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);

            dos.write(bytes);
            dos.writeBytes(dagVanVandaag);
            dos.flush();
            byte[] output = bos.toByteArray();

            //NU EFFECTIEF GAAN SIGNEN
            Signature signature = Signature.getInstance("SHA256WithDSA");
            signature.initSign(keyPairOfTheDay.getPrivate());
            signature.update(output);

            byte[] token = signature.sign();

            //STRING MAKEN DAWE KUNNEN COPY PASTEN
            String data = Base64.getEncoder().encodeToString(output);
            String signatureString = Base64.getEncoder().encodeToString(token);

            tokens.add(new Token(signatureString,data));
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
    public List<String> requestMonthlyNyms(String bussinesNumber) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        System.out.println("Create mothly nyms for: " + bussinesNumber);
        int aantalDagen = 10;
        List<String> monthlyNyms = new ArrayList<>();

        Date date = new Date();
        SimpleDateFormat df  = new SimpleDateFormat("ddMMMMyyyy");
        Calendar c1 = Calendar.getInstance();

        c1.add(Calendar.DAY_OF_YEAR,-10);   //Efkes terugkeren in de tijd om dingen te testen met incubatietijd

        for (int i = 0; i < aantalDagen; i++) {
            SecretKeySpec key;
            String nym;
            if (i == 0){
                String currentDate = df.format(date);
                //System.out.println(currentDate);
                key = generateDailyKey(bussinesNumber, currentDate);
                nym = createNym(key,bussinesNumber,currentDate);
                System.out.println("De key is: " + key + "\t De nym is: " + nym);
                mappingDayNyms.put(currentDate,nym);
            } else {
                //1 DAG AAN DE CALENDAR TOEVOEGEN
                c1.add(Calendar.DAY_OF_YEAR, 1);
                Date nextDate = c1.getTime();
                String dueDate = df.format(nextDate);
                //System.out.println(dueDate);
                key = generateDailyKey(bussinesNumber, dueDate);
                nym = createNym(key,bussinesNumber,dueDate);
                System.out.println("De key is: " + key + "\t De nym is: " + nym);
                mappingDayNyms.put(dueDate,nym);
            }
            //NIET VERGETEN TOE TE VOEGEN AAN DE ARRAY
            monthlyNyms.add(nym);
        }
        return monthlyNyms;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------//
}
