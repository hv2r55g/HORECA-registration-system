package registrar;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

public class Registrar implements RegistrarInterface {

    private SecretKey masterKey;

    public Registrar() {
        super();
    }


    public static void main(String[] args) throws NoSuchAlgorithmException {
        startRMIRegistry();
        String hostname = "localhost";
        String servicename = "RegistrarService";



        try {
            Registrar obj = new Registrar();
            obj.masterKey=obj.createMasterKey(); //KLOPT DIT ??
            RegistrarInterface stub = (RegistrarInterface) UnicastRemoteObject.exportObject(obj, 0);
            Naming.rebind("rmi://" + hostname + "/" + servicename, stub);
            System.out.println("RMI Server successful started");
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Server failed starting ...");
        }
    }

    public static void startRMIRegistry() {
        try {
            java.rmi.registry.LocateRegistry.createRegistry(1099);
            System.out.println("RMI Server ready");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getDailySecretKey(int businessNumber) throws RemoteException {
        //CREEER DAILY SECRET KEYS OP BASIS VAN DE DAG, BUSINESSNUMBER EN DE MASTER SECRET KEY
        byte[] aesKeyData = "test".getBytes();
        SecretKeySpec secretKey = new SecretKeySpec(aesKeyData, "AES");
    }

    public SecretKeySpec generateDailyKey(int businessNumber, Date date) throws RemoteException {

        SimpleDateFormat formatter = new SimpleDateFormat("ddMMMMyyyy");
        String strDate = formatter.format(date);

        String keyData = masterKey.toString()+Integer.toString(businessNumber)+strDate;
        byte[] aesKeyData = keyData.getBytes();
        return new SecretKeySpec(aesKeyData, "AES");
    }


    @Override
    public void generateDailyCustomerToken(int phoneNumber) throws RemoteException {
        SecretKeySpec[] tokens = new SecretKeySpec[48];
    }

    //TODO: moet dit geen array teruggeven? kweet dat jij met void werkt telkens?
    @Override
    public void requestMonthlyKeys(int bussinesNumber) throws RemoteException {

        SecretKeySpec[] monthlyKeys = new SecretKeySpec[30];
        for (int i = 0; i < 30; i++) {
            Date d = new Date(new Date().getTime() + 86400000*i);
            SecretKeySpec key = generateDailyKey(bussinesNumber, d);
            monthlyKeys[i] = key;
        }
    }

    public SecretKey getMasterKey() {
        return masterKey;
    }

    public void setMasterKey(SecretKey masterKey) {
        this.masterKey = masterKey;
    }

    public SecretKey createMasterKey() throws NoSuchAlgorithmException {
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        SecretKey aesKey = keygen.generateKey();
        return aesKey;
    }
}
