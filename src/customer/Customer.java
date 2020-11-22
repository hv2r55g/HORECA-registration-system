package customer;


import bar.Bar;
import bar.BarInterface;
import registrar.RegistrarInterface;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class Customer extends UnicastRemoteObject implements CustomerInterface {

    private static int phoneNumber;

    private RegistrarInterface registrarInterface;

    public Customer() throws RemoteException {
        super();
    }

    public Customer(int phoneNumber) throws RemoteException {
        super();
        Customer.phoneNumber = phoneNumber;
    }

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException {
        String hostname = "localhost";
        String clientService = "RegistrarListening";
        String servicename = "RegistrarService";
        Customer currentCustomer = new Customer();

        Naming.rebind("rmi://" + hostname + "/" + clientService, currentCustomer);
        RegistrarInterface registrarInterface = (RegistrarInterface) Naming.lookup("rmi://" + hostname + "/" + servicename);
        currentCustomer.registrarInterface = registrarInterface;

        //REQUEST MASTER SECRET KEY

        //Dit is nog maar random:
        Scanner sc = new Scanner(System.in);
        System.out.println("Geef uw gsm nummer: ");
        phoneNumber = sc.nextInt();

        //RANDOM NUMBER INPUT





    }

}
