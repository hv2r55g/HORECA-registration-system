package bar;

import registrar.RegistrarInterface;

import java.rmi.server.UnicastRemoteObject;
import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Bar extends UnicastRemoteObject implements BarInterface{

    private RegistrarInterface registrarInterface;

    public Bar() throws RemoteException {
        super();
    }

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException {

         String hostname = "localhost";
         String servicename = "RegistrarService";
         Bar currentBar = new Bar();

         Naming.rebind("rmi://" + hostname + "/" + servicename, currentBar);
         RegistrarInterface registrarInterface = (RegistrarInterface) Naming.lookup("rmi://" + hostname + "/" + servicename);
         currentBar.registrarInterface = registrarInterface;

         currentBar.registrarInterface.sendMessage("Hello World",currentBar);

    }
}
