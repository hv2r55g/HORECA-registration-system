package customer;


import mixingProxy.Capsule;
import mixingProxy.MixingProxyInterface;
import registrar.RegistrarInterface;

import java.net.MalformedURLException;
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
    private List<Bezoek> bezoeken;
    private RegistrarInterface registrarInterface;
    private MixingProxyInterface mixingProxyInterface;


    public Customer() throws RemoteException {
    }

    public Customer(String phoneNumber) throws RemoteException {
        super();
        this.phoneNumber = phoneNumber;
        this.bezoeken = new ArrayList();
    }

    private void requestTokens() throws RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        tokens = registrarInterface.requestDailyCustomerToken(phoneNumber);
    }

    private void bezoekBar(String PLAKDESTRINGHIER) throws RemoteException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        //BEZOEK LOKAAL OPSLAAN + CAPSULE DOORSTUREN NAAR MIXING PROXY
        String[] temp = PLAKDESTRINGHIER.split(";");
        Bezoek bezoek = new Bezoek(temp[0], temp[1], temp[2]);


        //CAPSULE OPMAKEN
        Capsule capsule = new Capsule(bezoek.getTimestamp(), bezoek.getDay(), tokens.get(0), bezoek.getHashBar());

        //sendCapsule is een boolean geworden die de checks gaat uitvoeren en true geeft als het gelukt is
        //wnr true gaat de mixing het opslaan en het signen
        //wnr true vragen we de sign op!!!
        mixingProxyInterface.sendCapsule(capsule);
        boolean doSign = mixingProxyInterface.sendCapsule(capsule);
        if (doSign){
            //dan moet men de sign ontvangen
            mixingProxyInterface.signCapsule(capsule);
            bezoeken.add(bezoek);
            System.out.println("Dit is de bytearray van de sign: "+mixingProxyInterface.signCapsule(capsule));
        } else{
            System.out.println("bezoek gefailed, waarschijnlijk door een check");
        }
        //GEBRUIKT TOKEN VERWIJDEREN
        tokens.remove(0);
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
        String PLAKDESTRINGHIER = "2662;1;����\u001C���J48U�\u0003�;9~\u001E�;";
        currentCustomer.bezoekBar(PLAKDESTRINGHIER);

    }

}
