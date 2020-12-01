package matchingService;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
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
import java.util.Collection;
import java.util.List;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

public class MatchingService implements MatchingServiceInterface, Remote{

    private RegistrarInterface registrarInterface;
    private List<Capsule> capsulesDB = new ArrayList<>();
    private ListMultimap<String, String> mappingDayNyms = ArrayListMultimap.create();
    public MatchingService(){super(); }

    public static void main(String[] args)  throws MalformedURLException, RemoteException, NotBoundException {

        startRMIRegistry();
        String hostname = "localhost";
        String servicename = "MatchingServiceService";
        String clientService = "RegistrarListening";
        String servicenameReg = "RegistrarService";

        try {
            MatchingService matchingService = new MatchingService();
            MatchingServiceInterface stub = (MatchingServiceInterface) UnicastRemoteObject.exportObject(matchingService, 0);
            Naming.rebind("rmi://" + hostname + "/" + servicename, stub);
            System.out.println("RMI Server Matching Service successful started");


            Naming.rebind("rmi://" + hostname + "/" + clientService, matchingService);
            RegistrarInterface registrarInterface = (RegistrarInterface) Naming.lookup("rmi://" + hostname + "/" + servicenameReg);
            matchingService.registrarInterface = registrarInterface;

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

    //Deze methode gaat de nyms gaan opvullen in een multimap!
    
    public void addNewNyms() throws RemoteException {
       ListMultimap<String, String> temp =registrarInterface.getMappingDayNyms();

        for (String key : temp.keys()) {
            Collection<String> values = temp.get(key);
            for (String value : values){
                mappingDayNyms.put(key,value);
            }
        }
    }

    //-----------------------------------------------OVERIDE METHODES VAN DE INTERFACE----------------------------------------------------------//
    @Override
    public void addCapsules(List<Capsule> nieuweCapsules) throws RemoteException {
        capsulesDB.addAll(nieuweCapsules);
    }
    //------------------------------------------------------------------------------------------------------------------------------------------//

}
