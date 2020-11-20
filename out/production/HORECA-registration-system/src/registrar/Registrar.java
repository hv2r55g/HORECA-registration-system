package registrar;

import javax.crypto.spec.SecretKeySpec;
import java.rmi.Naming;
import java.rmi.RemoteException;
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
                Registrar obj = new Registrar();
                RegistrarInterface stub = (RegistrarInterface) UnicastRemoteObject.exportObject(obj, 0);
                Naming.rebind("rmi://" + hostname + "/" + servicename, stub);
                System.out.println("RMI Server successful started");
            }
            catch(Exception e){
                System.out.println(e);
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
    public void getDailySecretKey(int businessNumber) throws RemoteException {
        //CREEER DAILY SECRET KEYS OP BASIS VAN DE DAG, BUSINESSNUMBER EN DE MASTER SECRET KEY
        
    }

    @Override
    public SecretKeySpec getMasterSecretKey() throws RemoteException {
        //GENERATE EEN MASTER SECRET KEY VOOR DE BAR
        byte[] aesKeyData = "test".getBytes();
        SecretKeySpec secretKey = new SecretKeySpec(aesKeyData, "AES");
        return secretKey;
    }

    @Override
    public void generateDailyCustomerToken(int phoneNumber) throws RemoteException {

    }
}
