package registrar;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Registrar implements RegistrarInterface {

    private SecretKey masterKey;

    public Registrar() throws NoSuchAlgorithmException {
        super();
        createMasterKey();
    }

    public SecretKey getMasterKey() {
        return masterKey;
    }

    public void setMasterKey(SecretKey masterKey) {
        this.masterKey = masterKey;
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        startRMIRegistry();
        String hostname = "localhost";
        String servicename = "RegistrarService";
        try {
            Registrar obj = new Registrar();
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

    //-------------------------------------------------------OVERIGE METHODES-------------------------------------------------------------------//

    public void createMasterKey() throws NoSuchAlgorithmException {
        //UNIEKE MASTERKEY VAN DE REGISTRAR GAAN GENEREREN, PER NIEUWE REGISTRAR DUS OOK NIEUWE MASTERKEY
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        SecretKey aesKey = keygen.generateKey();
        masterKey = aesKey;
    }

    public SecretKeySpec generateDailyKey(int businessNumber, Date date) throws RemoteException {

        SimpleDateFormat formatter = new SimpleDateFormat("ddMMMMyyyy");
        String strDate = formatter.format(date);

        String keyData = masterKey.toString()+Integer.toString(businessNumber)+strDate;
        byte[] aesKeyData = keyData.getBytes();
        return new SecretKeySpec(aesKeyData, "AES");
    }

    //------------------------------------------------------------------------------------------------------------------------------------------//

    //-----------------------------------------------OVERIDE METHODES VAN DE INTERFACE----------------------------------------------------------//

    @Override
    public void requestDailyCustomerToken(int phoneNumber) throws RemoteException {
        SecretKeySpec[] tokens = new SecretKeySpec[48];
    }

    @Override
    public SecretKeySpec[] requestMonthlyKeys(int bussinesNumber) throws RemoteException {
        SecretKeySpec[] monthlyKeys = new SecretKeySpec[30];
        for (int i = 0; i < 30; i++) {
            Date d = new Date(new Date().getTime() + 86400000*i);
            SecretKeySpec key = generateDailyKey(bussinesNumber, d);
            monthlyKeys[i] = key;
        }
        return monthlyKeys;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------//
}
