package customer;


import mixingProxy.Capsule;
import mixingProxy.MixingProxyInterface;
import registrar.RegistrarInterface;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Customer extends UnicastRemoteObject implements CustomerInterface {
    private String phoneNumber;
    private List<byte[]> tokens;
    private byte[] currentToken;
    private List<Bezoek> bezoeken;
    private String[] QRcodeCurrentBar;
    private RegistrarInterface registrarInterface;
    private MixingProxyInterface mixingProxyInterface;


    public Customer() throws RemoteException {}

    public Customer(String phoneNumber) throws RemoteException {
        this.phoneNumber = phoneNumber;
        this.bezoeken = new ArrayList();
        QRcodeCurrentBar = new String[3];
    }

    private void requestTokens() throws RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        tokens = registrarInterface.requestDailyCustomerToken(phoneNumber);
    }

    private void bezoekBar() throws RemoteException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        if (!tokens.isEmpty()) {
            //CAPSULE OPMAKEN
            currentToken = tokens.get(0);
            Capsule capsule = new Capsule(currentToken, QRcodeCurrentBar[2]);

            //sendCapsule is een boolean geworden die de checks gaat uitvoeren en true geeft als het gelukt is
            //wnr true gaat de mixing het opslaan en het signen
            //wnr true vragen we de sign op!!!
            boolean doSign = mixingProxyInterface.sendCapsule(capsule);
            if (doSign) {
                //dan moet men de sign ontvangen
                byte[] signedCapsule = mixingProxyInterface.signCapsule(capsule);
                System.out.println("Dit is de bytearray van de sign: " + signedCapsule);
            } else {
                System.out.println("bezoek gefailed, waarschijnlijk door een check");
            }
            //GEBRUIKT TOKEN VERWIJDEREN
            tokens.remove(0);
        } else {
            System.out.println("U kan deze bar helaas niet meer bezoeken, uw tokens voor vandaag zijn verbruikt");
        }
    }

    private void verlaatBar() throws RemoteException {
        //LEAVING TIME IN CAPSULE GAAN FIXEN, TERGELIJK NOG KEER DAT OBECT TERUGSTUREN VOOR ONS BEZOEK AAN TE MAKEN
        Capsule currentCapsule = mixingProxyInterface.requestLeaving(currentToken);
        //BEZOEK LOKAAL OPSLAAN
        Bezoek bezoek = new Bezoek(currentCapsule.getTimestampEntered(),currentCapsule.getTimestampLeaving(),QRcodeCurrentBar[0],QRcodeCurrentBar[1],QRcodeCurrentBar[2]);
        bezoeken.add(bezoek);   //hoeft niet mer perse
        sendToLocalDatabase(bezoek);
    }

    private void sendToLocalDatabase(Bezoek bezoek){
        try {
            System.out.println("Voeg bezoek toe aan local database");
            String path = "src/DoktersBestanden/";
            String fileName = phoneNumber + ".csv";
            File file = new File(path+fileName);

            FileWriter fileWritter = new FileWriter(path+fileName,true);
            BufferedWriter bw = new BufferedWriter(fileWritter);
            Scanner sc = new Scanner(file);

            if(!sc.hasNextLine()) {
                System.out.println("File bestaat nog niet");
                bw.append("Timestamp entering;Timestamp leaving;Random number bar;Bussiness number bar;Hash bar");
                bw.newLine();
                bw.append(bezoek.getTimestampEntered() + ";" + bezoek.getTimestampLeaving()+ ";" + bezoek.getRandomIntBar()+ ";" + bezoek.getBusinessNumberBar()+ ";" + bezoek.getHashBar());
                bw.newLine();
            } else {
                System.out.println("File bestaat wel al");
                bw.append(bezoek.getTimestampEntered() + ";" + bezoek.getTimestampLeaving()+ ";" + bezoek.getRandomIntBar()+ ";" + bezoek.getBusinessNumberBar()+ ";" + bezoek.getHashBar());
                bw.newLine();
            }
            sc.close();
            bw.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    private void clearLocalDatabase() throws IOException {
        System.out.println("Clear local database");
        String path = "src/DoktersBestanden/";
        String fileName = phoneNumber + ".csv";

        FileWriter fileWritter = new FileWriter(path+fileName);
        BufferedWriter bw = new BufferedWriter(fileWritter);
        bw.write("");
        bw.close();
    }

    public static void main(String[] args) throws IOException, NotBoundException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidKeySpecException {
        //Scanner sc = new Scanner(System.in);
        //System.out.println("Geef uw gsm nummer: ");
        //int phoneNumber = sc.nextInt();
        String phoneNumber = "123456789";
        Customer currentCustomer = new Customer(phoneNumber);

        //CONNECTEN MET REGISTRAR
        String hostname = "localhost";
        String clientService = "RegistrarListening";
        String servicename = "RegistrarService";
        Naming.rebind("rmi://" + hostname + "/" + clientService, currentCustomer);
        RegistrarInterface registrarInterface = (RegistrarInterface) Naming.lookup("rmi://" + hostname + "/" + servicename);
        currentCustomer.registrarInterface = registrarInterface;

        //CONNECTEN MET MIXING PROXY
        clientService = "MixingProxyListening";
        servicename = "MixingProxyService";
        Naming.rebind("rmi://" + hostname + "/" + clientService, currentCustomer);
        MixingProxyInterface mixingProxyInterface = (MixingProxyInterface) Naming.lookup("rmi://" + hostname + "/" + servicename);
        currentCustomer.mixingProxyInterface = mixingProxyInterface;

        //GUI KAN GESTART WORDEN MET ONDERSTAANDE LIJN
        //CustomerGUI app = new CustomerGUI(args);

        //1 AANVRAAG (VAN 48 TOKENS) PER DAG, IEDERE TOKEN KAN MAAR 1 KEER GEBRUIKT WORDEN
        currentCustomer.requestTokens();

        //WE BEZOEKEN EEN BAR, TIJDELIJK PLAKKEN WE HIER ALLE QR CODE INFO
        String PLAKDESTRINGHIER = "1477;1;��,\u001E�k��Ks���\u001CO�iD�;";
        //KAN MISSCHIEN IN METHODKE VO CLEANER
        String[] temp = PLAKDESTRINGHIER.split(";");
        currentCustomer.QRcodeCurrentBar[0] = temp[0];
        currentCustomer.QRcodeCurrentBar[1] = temp[1];
        currentCustomer.QRcodeCurrentBar[2] = temp[2];


        //SIMULATIE BEZOEKEN
        currentCustomer.bezoekBar();
        currentCustomer.verlaatBar();
        currentCustomer.bezoekBar();
        currentCustomer.verlaatBar();

        //METHODE OM KEER HEEL DIE FILE TE CLEAREN
        //currentCustomer.clearLocalDatabase();


    }
}
