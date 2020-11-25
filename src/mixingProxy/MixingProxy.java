package mixingProxy;

import registrar.Registrar;
import registrar.RegistrarInterface;

import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class MixingProxy implements MixingProxyInterface, Remote {
    private List<Capsule> capsules = new ArrayList<>();

    public MixingProxy(){}

    public static void main(String[] args) {
        startRMIRegistry();
        String hostname = "localhost";
        String servicename = "MixingProxyService";
        try {
            MixingProxy mixingProxy = new MixingProxy();
            MixingProxyInterface stub = (MixingProxyInterface) UnicastRemoteObject.exportObject(mixingProxy, 0);
            Naming.rebind("rmi://" + hostname + "/" + servicename, stub);
            System.out.println("RMI Server Mixing Proxy successful started");
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Server failed starting ...");
        }
    }

    public static void startRMIRegistry() {
        try {
            java.rmi.registry.LocateRegistry.createRegistry(2099);
            System.out.println("RMI Server Mixing proxy ready");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    //-------------------------------------------------------OVERIGE METHODES-------------------------------------------------------------------//

    //------------------------------------------------------------------------------------------------------------------------------------------//



    //-----------------------------------------------OVERIDE METHODES VAN DE INTERFACE----------------------------------------------------------//

    @Override
    public void sendCapsule(Capsule capsule) throws RemoteException {
        capsules.add(capsule);
    }

    //------------------------------------------------------------------------------------------------------------------------------------------//

}
