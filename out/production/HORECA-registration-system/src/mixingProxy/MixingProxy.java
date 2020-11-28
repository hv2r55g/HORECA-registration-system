package mixingProxy;

import registrar.RegistrarInterface;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.rmi.RemoteException;


public class MixingProxy implements MixingProxyInterface, Remote {
    private List<Capsule> capsules = new ArrayList<>();

    //Werken met een queue, dan kan je aan de hand van queue.poll het eerste element retrieven EN REMOVEN.
    //Queue<Capsule> queueCapsules = new LinkedList<Capsule>();
    private RegistrarInterface registrarInterface;
    private PublicKey publicKeyToday;
    private PrivateKey privateKeyToday;
    private String day;

    public MixingProxy() {
        super();
    }

    public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException {

        MixingProxy mixingProxyBind = new MixingProxy();

        startRMIRegistry();
        String hostname = "localhost";
        String servicename = "MixingProxyService";
        String clientService = "RegistrarListening";
        String servicenameReg = "RegistrarService";

        try {
            MixingProxy mixingProxy = new MixingProxy();
            MixingProxyInterface stub = (MixingProxyInterface) UnicastRemoteObject.exportObject(mixingProxy, 0);
            Naming.rebind("rmi://" + hostname + "/" + servicename, stub);
            System.out.println("RMI Server Mixing Proxy successful started");


            Naming.rebind("rmi://" + hostname + "/" + clientService, mixingProxy);
            RegistrarInterface registrarInterface = (RegistrarInterface) Naming.lookup("rmi://" + hostname + "/" + servicenameReg);

            mixingProxy.registrarInterface = registrarInterface;
            mixingProxy.getPublicKey();

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


    public boolean checkValidityToken(Capsule capsule) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidKeySpecException {
        boolean isValid = false;
        Date date = new Date();
        SimpleDateFormat df  = new SimpleDateFormat("ddMMMMyyyy");
        String dagVanVandaag = df.format(date);

        Signature signatureVerify = Signature.getInstance("SHA256WithDSA");
        signatureVerify.initVerify(publicKeyToday);
        signatureVerify.update(dagVanVandaag.getBytes());

        isValid = signatureVerify.verify(capsule.getTokenCustomer());

        System.out.println("the token is: " + isValid);
        return isValid;
    }

    public boolean checkDayOfToken(Capsule capsule) throws RemoteException {
        boolean dayOk = false;
        Date date = new Date();
        SimpleDateFormat df  = new SimpleDateFormat("ddMMMMyyyy");
        String dagVanVandaag = df.format(date);
        if (capsule.getDagBezoek().equals(dagVanVandaag)) {
            dayOk = true;
        }


        return dayOk;
    }

    public boolean checkTokenNotSpendYet(Capsule capsule) {
        boolean isNewToken = true;


        if (capsules.isEmpty()) {
            return isNewToken;
        } else {
            for (Capsule capsuleInLijst : capsules) {
                if (capsule.getTokenCustomer().equals(capsuleInLijst.getTokenCustomer())) {
                    isNewToken = false;
                    break;
                }
            }
        }


        return isNewToken;
    }

    public void getPublicKey() throws RemoteException {
        publicKeyToday = registrarInterface.getPublicKeyOfTheDay();
    }

    public void getPrivateKey() throws RemoteException {
        privateKeyToday = registrarInterface.getPrivatekeyOftheDay();
    }



    //------------------------------------------------------------------------------------------------------------------------------------------//


    //-----------------------------------------------OVERIDE METHODES VAN DE INTERFACE----------------------------------------------------------//

    @Override
    public synchronized boolean sendCapsule(Capsule capsule) throws RemoteException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        System.out.println("Capsule ontvangen, nu checks doen");
        boolean checksOK = false;
        System.out.println(capsule.toString());
        //queueCapsules.add(capsule);

        if (checkDayOfToken(capsule) && checkTokenNotSpendYet(capsule) && checkValidityToken(capsule)) {
            System.out.println("woehoe alle checks oke");
            capsules.add(capsule);

            checksOK = true;
        }

        return checksOK;
    }

    @Override
    public byte[] signCapsule(Capsule capsule) throws NoSuchAlgorithmException, SignatureException, RemoteException, InvalidKeyException {

        Signature signature = Signature.getInstance("SHA256WithDSA");
        SecureRandom secureRandom = new SecureRandom();
        signature.initSign(registrarInterface.getPrivatekeyOftheDay(), secureRandom);
        signature.update(capsule.getTokenCustomer());
        byte[] signedToken = signature.sign();

        return signedToken;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------//

}
