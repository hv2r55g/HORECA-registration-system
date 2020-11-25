package customer;


import registrar.RegistrarInterface;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.List;

public class Customer extends UnicastRemoteObject implements CustomerInterface {
    private String phoneNumber;
    private List tokens;
    private String[] QRCodeInfo = new String[3];
    private RegistrarInterface registrarInterface;


    public Customer() throws RemoteException {
        super();
    }

    public Customer(String phoneNumber) throws RemoteException {
        super();
        this.phoneNumber = phoneNumber;
    }

    private void requestTokens() throws RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        tokens = registrarInterface.requestDailyCustomerToken(phoneNumber);
    }

    private void requestQRCodeInfo(String PLAKDESTRINGHIER) throws RemoteException {
        String[] temp = PLAKDESTRINGHIER.split(";");
        for (int i = 0; i < temp.length; i++) {
            QRCodeInfo[i] = temp[i];
        }
    }

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
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

        //GUI KAN GESTART WORDEN MET ONDERSTAANDE LIJN
        //CustomerGUI app = new CustomerGUI(args);

        //1 AANVRAAG (VAN 48 TOKENS) PER DAG, IEDERE TOKEN KAN MAAR 1 KEER GEBRUIKT WORDEN
        currentCustomer.requestTokens();

        //WE BEZOEKEN EEN BAR, NORMAAL ONTVANGEN WE DE GEGEVENS VAN DE QR CODE
        //AANGEZIEN WE GEEN APP MAKEN, RMI OPZETTEN TUSSEN BAR EN CUSTOMER VOOR DIE GEGEVENS OP TE VRAGEN
        //DEZE COMMUNICATIE DIENT ENKEL MAAR OP DIE DAG GEGEVENS OP TE VRAGEN
        String PLAKDESTRINGHIER = "";
        currentCustomer.requestQRCodeInfo(PLAKDESTRINGHIER);



    }

}
