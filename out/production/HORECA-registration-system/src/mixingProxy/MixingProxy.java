package mixingProxy;

import matchingService.MatchingServiceInterface;
import registrar.RegistrarInterface;

import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;


public class MixingProxy implements MixingProxyInterface, Remote {
    private List<Capsule> capsules = new ArrayList<>();

    //Werken met een queue, dan kan je aan de hand van queue.poll het eerste element retrieven EN REMOVEN.
    //Queue<Capsule> queueCapsules = new LinkedList<Capsule>();
    private RegistrarInterface registrarInterface;
    private MatchingServiceInterface matchingServiceInterface;
    private PublicKey publicKeyToday;
    private PrivateKey privateKeyToday;
    private String dagVanVandaag;

    public MixingProxy() {
        super();
    }

    public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException, InterruptedException, ParseException {
        startRMIRegistry();
        String hostname = "localhost";
        String servicename = "MixingProxyService";
        String clientService = "RegistrarListening";
        String servicenameReg = "RegistrarService";
        String servicenameMatchingServer = "MatchingServiceService";

        MixingProxy mixingProxy = new MixingProxy();
        try {

            MixingProxyInterface stub = (MixingProxyInterface) UnicastRemoteObject.exportObject(mixingProxy, 0);
            Naming.rebind("rmi://" + hostname + "/" + servicename, stub);
            System.out.println("RMI Server Mixing Proxy successful started");

            //CONNECTEN MET REGISTRAR
            Naming.rebind("rmi://" + hostname + "/" + clientService, mixingProxy);
            RegistrarInterface registrarInterface = (RegistrarInterface) Naming.lookup("rmi://" + hostname + "/" + servicenameReg);
            mixingProxy.registrarInterface = registrarInterface;

            //CONNECTEN MET MATICHING SERVICE
            clientService = "MatchingServiceListening";
            Naming.rebind("rmi://" + hostname + "/" + clientService, mixingProxy);
            MatchingServiceInterface matchingServiceInterface = (MatchingServiceInterface) Naming.lookup("rmi://" + hostname + "/" + servicenameMatchingServer);
            mixingProxy.matchingServiceInterface = matchingServiceInterface;

            //DAG VAN VANDAAG INSTELLEN
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("ddMMMMyyyy");
            mixingProxy.dagVanVandaag = sdf.format(date);


            mixingProxy.getPublicKey();



        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Server failed starting ...");
        }
        //Na 2 minuten keer flushen
        TimeUnit.MINUTES.sleep(2);
        mixingProxy.sendCapsulesToMatchingService();

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
        Date date = new Date();
        SimpleDateFormat df  = new SimpleDateFormat("ddMMMMyyyy");
        String dagVanVandaag = df.format(date);

        Signature signatureVerify = Signature.getInstance("SHA256WithDSA");
        signatureVerify.initVerify(publicKeyToday);
        signatureVerify.update(dagVanVandaag.getBytes());

        boolean isValid = signatureVerify.verify(capsule.getTokenCustomer());
        return isValid;
    }

    public boolean checkDayOfToken(Capsule capsule) throws RemoteException {
        boolean dayOk = false;
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
            //TODO: Voldoende enkel de huidige capsules te checken?
            for (Capsule capsuleInLijst : capsules) {
                if (Arrays.equals(capsule.getTokenCustomer(),capsuleInLijst.getTokenCustomer())) {
                    isNewToken = false;
                    break;
                }
            }
        }
        System.out.println("Is het een nieuwe token? " + isNewToken);
        return isNewToken;
    }

    public void getPublicKey() throws RemoteException {
        publicKeyToday = registrarInterface.getPublicKeyOfTheDay();
    }

    public void getPrivateKey() throws RemoteException {
        privateKeyToday = registrarInterface.getPrivatekeyOftheDay();
    }

    public void sendCapsulesToMatchingService() throws RemoteException, ParseException {
        System.out.println("Flushen van de capsules");
        //CAPSULES MOETEN EERST GESHUFFELD WORDEN
        List<Capsule> toMatchingCapsules = new ArrayList<>();

        //NA 23U55 AANGEPASTE REGELING
        Date date = new Date() ;
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm") ;
        dateFormat.format(date);
        String sluitingsuur = "23:55";

        if (dateFormat.parse(dateFormat.format(date)).before(dateFormat.parse(sluitingsuur))){
            int aantalCapsulesOorspronkelijk = capsules.size();
            for (int i = aantalCapsulesOorspronkelijk-1; i >= 0; i--) {
                Capsule currentCapsule = capsules.get(i);
                if (currentCapsule.getTimestampLeaving()!=0){
                    //CAPSULE NAAR MATCHING EN VERWIJDEREN UIT MIXING
                    toMatchingCapsules.add(currentCapsule);
                    capsules.remove(currentCapsule);
                }
            }
        } else {
            //NA 23U55
            int aantalCapsulesOorspronkelijk = capsules.size();
            for (int i = aantalCapsulesOorspronkelijk-1; i>=0; i--) {
                Capsule currentCapsule = capsules.get(i);
                if (currentCapsule.getTimestampLeaving()==0){
                    currentCapsule.setTimestampLeaving(System.currentTimeMillis()); //Ofwel het sluitingsuur
                }
                //OVERIGE CAPSULES NAAR MATCHING EN VERWIJDEREN UIT MIXING
                toMatchingCapsules.add(currentCapsule);
                capsules.remove(currentCapsule);
            }
            //TO BE SURE
            System.out.println("Zijn er nog capsules aanwezig na 23u55?" + capsules.size());
            capsules = new ArrayList<>();
        }

        System.out.println("Nog zoveel capsules aanwezig: " + capsules.size());
        System.out.println("Zoveel capsules naar matiching: " + toMatchingCapsules.size());
        Collections.shuffle(toMatchingCapsules);
        matchingServiceInterface.addCapsules(toMatchingCapsules);

    }



    //------------------------------------------------------------------------------------------------------------------------------------------//


    //-----------------------------------------------OVERIDE METHODES VAN DE INTERFACE----------------------------------------------------------//

    @Override
    public synchronized boolean sendCapsule(Capsule capsule) throws RemoteException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        //TIJDEN VAN DE CAPSULES GAAN TOEVOEGEN
        capsule.setDagBezoek(dagVanVandaag);
        capsule.setTimestampEntered(System.currentTimeMillis());

        if (checkDayOfToken(capsule) && checkTokenNotSpendYet(capsule) && checkValidityToken(capsule)) {
            System.out.println("All checks oke");
            capsules.add(capsule);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public byte[] signCapsule(Capsule capsule) throws NoSuchAlgorithmException, SignatureException, RemoteException, InvalidKeyException {
        Signature signature = Signature.getInstance("SHA256WithDSA");
        SecureRandom secureRandom = new SecureRandom();
        signature.initSign(registrarInterface.getPrivatekeyOftheDay(), secureRandom);   //TODO: dit mag niet volgens mij
        signature.update(capsule.getTokenCustomer());
        byte[] signedToken = signature.sign();

        return signedToken;
    }

    @Override
    public void requestLeaving(byte[] currentToken) throws RemoteException {
        //TOKEN GAAN ZOEKEN EN EIND TIMESTAMP AAN TOEVOEGEN
        for (Capsule c: capsules){
            if (Arrays.equals(c.getTokenCustomer(),currentToken)){
                //System.out.println("Capsule voor: " + c.toString());
                c.setTimestampLeaving(System.currentTimeMillis());
                //System.out.println("Capsule na: " + c.toString());
                //NORMAAL ZAL TOKEN UNIEK ZIJN EN MAG DE FORLOOP STOPPEN
                break;
            }
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------//

}
