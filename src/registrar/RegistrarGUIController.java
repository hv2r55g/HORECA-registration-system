package registrar;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import mixingProxy.Capsule;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class RegistrarGUIController extends UnicastRemoteObject implements RegistrarInterface{
    @FXML
    ListView listViewNyms;

    @FXML
    ListView listViewTokens;

    private int aantalTokensPerCustomer;
    private String dagVanVandaag;
    private ListMultimap<String, String> mappingDayNyms = ArrayListMultimap.create();
    private Map<String, List<Token>> mappingTokens;
    private KeyPair keyPairOfTheDay;
    private SecretKey masterKey;

    private ObservableList<String> stringListNyms;
    private ObservableList<String> stringListTokens;

    public RegistrarGUIController() throws RemoteException {
        super();
    }

    public void initController() throws NoSuchAlgorithmException {
        initAttributen();
        initConnecties();
        initTable();
    }

    private void initTable() {
        listViewNyms.setItems(stringListNyms);
        listViewTokens.setItems(stringListTokens);
    }

    private void initConnecties() {
        startRMIRegistry();
        String hostname = "localhost";
        String servicename = "RegistrarService";
        String clientService = "MatchingServiceListening";
        String servicenameMatchingServer = "MatchingServiceService";
        try {
            Naming.rebind("rmi://" + hostname + "/" + servicename, this);
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

    private void initAttributen() throws NoSuchAlgorithmException {
        createMasterKey();
        aantalTokensPerCustomer = 20;
        mappingTokens = new HashMap<>();
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");
        keyPairOfTheDay = keyPairGenerator.generateKeyPair();
        Date date = new Date();
        SimpleDateFormat df  = new SimpleDateFormat("ddMMMMyyyy");
        dagVanVandaag = df.format(date);

        stringListNyms = FXCollections.observableArrayList();
        stringListTokens = FXCollections.observableArrayList();
    }

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
    }

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
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                refreshListViewTokens();
            }
        });


        //AT THE END TOKENS NAAR GEBRUIKER STUREN
        return tokens;
    }

    private void refreshListViewTokens() {
        stringListTokens.clear();
        List<String> nieuweGegevens = new ArrayList<>();

        //Nu alles in de map overlopen en naar string vormen
        for (String phoneNumber: mappingTokens.keySet()){
            StringBuilder sb = new StringBuilder();
            sb.append("<" + phoneNumber + ">: {");
            List<Token> temp = mappingTokens.get(phoneNumber);
            for (int i = 0; i < temp.size() ; i++) {
                Token currentToken = temp.get(i);
                sb.append(" [" + currentToken.getSignature() + "\t\t\t" + currentToken.getDatumInfo() + "] ");
            }
            sb.append("}");
            nieuweGegevens.add(sb.toString());
        }
        stringListTokens.addAll(nieuweGegevens);
    }

    @Override
    public PublicKey getPublicKeyOfTheDay() {
        return keyPairOfTheDay.getPublic();
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
        System.out.println();
        System.out.println();
        System.out.println("Alle keys van de incubatietijd");
        for (String s : result.keySet()) {
            System.out.println("-------------------------------------------------------------------------");
            System.out.println("Key: " + s);
            for (String hash: result.get(s)){
                System.out.println("Value: " + hash);
            }
        }
        System.out.println();
        System.out.println();

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
                //System.out.println("De key is: " + key + "\t De nym is: " + nym);
                mappingDayNyms.put(currentDate,nym);
            } else {
                //1 DAG AAN DE CALENDAR TOEVOEGEN
                c1.add(Calendar.DAY_OF_YEAR, 1);
                Date nextDate = c1.getTime();
                String dueDate = df.format(nextDate);
                //System.out.println(dueDate);
                key = generateDailyKey(bussinesNumber, dueDate);
                nym = createNym(key,bussinesNumber,dueDate);
                //System.out.println("De key is: " + key + "\t De nym is: " + nym);
                mappingDayNyms.put(dueDate,nym);
            }
            //NIET VERGETEN TOE TE VOEGEN AAN DE ARRAY
            monthlyNyms.add(nym);
        }

        //TOT SLOT ONZE LISTVIEW GAAN REFRESHEN
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                refreshListViewNyms();
            }
        });

        return monthlyNyms;
    }

    private void refreshListViewNyms() {
        stringListNyms.clear();
        List<String> nieuweGegevens = new ArrayList<>();

        for (String datum: mappingDayNyms.keySet()){
            StringBuilder sb = new StringBuilder();
            sb.append("<" + datum + ">: {");
            List<String> nyms = mappingDayNyms.get(datum);
            for (int i = 0; i < nyms.size(); i++) {
                sb.append(nyms.get(i));
                if (i!=(nyms.size()-1)){
                    sb.append("\t\t\t");
                }
            }
            sb.append("}");
            nieuweGegevens.add(sb.toString());
        }
        stringListNyms.addAll(nieuweGegevens);
    }


    @Override
    public void sendUninformedCustomers(List<Capsule> uninformedCapsules) {
        for (Capsule capsule : uninformedCapsules){
            for (String key : mappingTokens.keySet()){
                for (Token token : mappingTokens.get(key)){
                    if (capsule.getTokenCustomer().getSignature().equals(token.getSignature())){
                        System.out.println("De persoon van deze token moet geïnformeerd worden: " + key);
                    }
                }
            }
        }
    }
}
