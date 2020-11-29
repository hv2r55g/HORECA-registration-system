package matchingService;

import mixingProxy.Capsule;
import mixingProxy.MixingProxy;
import mixingProxy.MixingProxyInterface;
import registrar.Registrar;
import registrar.RegistrarInterface;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MatchingService implements MatchingServiceInterface, Remote{

    private List<Capsule> capsulesDB = new ArrayList<>();

    public MatchingService(){ }

    public static void main(String[] args)  throws MalformedURLException, RemoteException, NotBoundException {

        startRMIRegistry();
        String hostname = "localhost";
        String servicename = "MatchingServiceService";
        try {
            MatchingService matchingService = new MatchingService();
            MatchingServiceInterface stub = (MatchingServiceInterface) UnicastRemoteObject.exportObject(matchingService, 0);
            Naming.rebind("rmi://" + hostname + "/" + servicename, stub);
            System.out.println("RMI Server Matching Service successful started");
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Server failed starting ...");
        }

    }

    public static void startRMIRegistry() {
        try {
            java.rmi.registry.LocateRegistry.createRegistry(3099);
            System.out.println("RMI Server Matching Service ready");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    //-------------------------------------------------------OVERIGE METHODES-------------------------------------------------------------------//
    //------------------------------------------------------------------------------------------------------------------------------------------//


    //-----------------------------------------------OVERIDE METHODES VAN DE INTERFACE----------------------------------------------------------//
    @Override
    public void addCapsules(List<Capsule> nieuweCapsules) throws RemoteException {
        capsulesDB.addAll(nieuweCapsules);
    }
    //------------------------------------------------------------------------------------------------------------------------------------------//

}
