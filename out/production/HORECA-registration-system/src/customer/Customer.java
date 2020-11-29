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
            //mixingProxyInterface.sendCapsule(capsule);
            boolean doSign = mixingProxyInterface.sendCapsule(capsule);
            if (doSign) {
                //dan moet men de sign ontvangen
                //TODO: moet signature opgeslagen worden?
                mixingProxyInterface.signCapsule(capsule);
                System.out.println("Dit is de bytearray van de sign: " + mixingProxyInterface.signCapsule(capsule));
            } else {
                //TODO: Hier wss nog kiezen voor een andere token proberen
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
        bezoeken.add(bezoek);
    }

    private void stuurGegevensNaarDokter() {
        try{
            String path = "src/DoktersBestanden/";
            String fileName = phoneNumber + ".csv";
            File file = new File(path+fileName);
            BufferedWriter bf = new BufferedWriter(new FileWriter(file));
            bf.append("Timestamp entering;Timestamp leaving;Random number bar;Bussiness number bar;Hash bar");
            bf.newLine();
            for (Bezoek b : bezoeken){
                bf.append(b.getTimestampEntered() + ";" + b.getTimestampLeaving()+ ";" + b.getRandomIntBar()+ ";" + b.getBusinessNumberBar()+ ";" + b.getHashBar());
                bf.newLine();
            }
            bf.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidKeySpecException {
        //Scanner sc = new Scanner(System.in);
        //System.out.println("Geef uw gsm nummer: ");
        //int phoneNumber = sc.nextInt();
        String phoneNumber = "0476836000";
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

        //DB NAAR DE DOKTER
        currentCustomer.stuurGegevensNaarDokter();



    }
}
