package customer;


import bar.Bar;
import bar.BarInterface;
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
import java.util.Scanner;

public class Customer extends UnicastRemoteObject implements CustomerInterface {
    private String phoneNumber;
    private List tokens;
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

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        //Scanner sc = new Scanner(System.in);
        //System.out.println("Geef uw gsm nummer: ");
        //int phoneNumber = sc.nextInt();
        String phoneNumber = "0476836000";
        Customer currentCustomer = new Customer(phoneNumber);

        //RMI OPZETTEN
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

        //WE BEZOEKEN EEN BAR



    }

}
