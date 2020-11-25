package bar;

import registrar.RegistrarInterface;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.rmi.server.UnicastRemoteObject;
import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class Bar extends UnicastRemoteObject implements BarInterface{

    private int bussinesNumber;
    private SecretKeySpec[] mothlyKeys;
    private RegistrarInterface registrarInterface;

    public Bar() throws RemoteException {
        super();
    }

    public Bar(int bussinesNumber) throws RemoteException {
        super();
        this.bussinesNumber = bussinesNumber;
    }

    private void requestMonthlyKeys() throws RemoteException{
        mothlyKeys = registrarInterface.requestMonthlyKeys(bussinesNumber);
    }

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException {
        //HANDMATIGE INPUT
        Scanner sc = new Scanner(System.in);
        System.out.println("Geef een bussiness number: ");
        int bussinesNumberFromScanner = sc.nextInt();

        //RANDOM NUMBER INPUT
        //TODO: Als we ooit niet steeds dat bussiness number willen ingeven

        Bar currentBar = new Bar(bussinesNumberFromScanner);

        //RMI OPZETTEN
        String hostname = "localhost";
        String clientService = "RegistrarListening";
        String servicename = "RegistrarService";
        Naming.rebind("rmi://" + hostname + "/" + clientService, currentBar);
        RegistrarInterface registrarInterface = (RegistrarInterface) Naming.lookup("rmi://" + hostname + "/" + servicename);
        currentBar.registrarInterface = registrarInterface;

        //1 KEER PER MAAND DE KEYS OPVRAGEN
        currentBar.requestMonthlyKeys();
    }
}
