package registrar;

import bar.Bar;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteRef;
import java.rmi.server.UnicastRemoteObject;

public class Registrar implements RegistrarInterface{

    public Registrar(){
        super();
    }



    public static void main(String[] args) {
            startRMIRegistry();
            String hostname = "localhost";
            String servicename = "RegistrarService";

            try{
                RegistrarInterface hello = new Registrar();
                Naming.rebind("rmi://" + hostname + "/" + servicename, hello);
                System.out.println("RMI Server successful started");
            }
            catch(Exception e){
                System.out.println("Server failed starting ...");
            }
        }

        public static void startRMIRegistry() {
            try{
                java.rmi.registry.LocateRegistry.createRegistry(1099);
                System.out.println("RMI Server ready");
            }
            catch(RemoteException e) {
                e.printStackTrace();
            }
        }

    @Override
    public void sendMessage(String message, Bar bar) {
        System.out.println(message);
    }
}
