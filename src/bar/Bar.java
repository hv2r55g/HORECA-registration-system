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

    private static int bussinesNumber;
    private RegistrarInterface registrarInterface;


    public Bar(int bussinesNumber) throws RemoteException {
        super();
        Bar.bussinesNumber = bussinesNumber;
    }

    public Bar() throws RemoteException {
        super();
    }

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException {
        String hostname = "localhost";
        String clientService = "RegistrarListening";
        String servicename = "RegistrarService";
        Bar currentBar = new Bar();

        Naming.rebind("rmi://" + hostname + "/" + clientService, currentBar);
        RegistrarInterface registrarInterface = (RegistrarInterface) Naming.lookup("rmi://" + hostname + "/" + servicename);
        currentBar.registrarInterface = registrarInterface;

        //REQUEST MASTER SECRET KEY

        //HANDMATIGE INPUT
        Scanner sc = new Scanner(System.in);
        System.out.println("Geef een bussiness number: ");
        bussinesNumber = sc.nextInt();

        //RANDOM NUMBER INPUT


        currentBar.getDailySecretKey(bussinesNumber);


    }


    private void getDailySecretKey(int bussinesNumber) throws RemoteException {
        //OMGAAN MET DE SECRET KEYS
        registrarInterface.getDailySecretKey(bussinesNumber);
    }

    private void requestMonthlyKeys(int bussinesNumber) throws RemoteException{
        registrarInterface.requestMonthlyKeys(bussinesNumber);
    }

}
