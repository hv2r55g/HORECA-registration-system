package matchingService;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import customer.Bezoek;
import doctor.CriticalTuple;
import mixingProxy.Capsule;
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
import java.util.concurrent.TimeUnit;

public class MatchingService implements MatchingServiceInterface, Remote{

    private RegistrarInterface registrarInterface;
    private List<Capsule> capsulesDB = new ArrayList<>();
    private List<Capsule> infectedCapsules = new ArrayList<>();
    private List<CriticalTuple> criticalTuples = new ArrayList<>();
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

            matchingService.addNewNyms();
//            TimeUnit.MINUTES.sleep(1);
//            matchingService.addNewNyms();
//            //TER CONTROLE KEER ALLE KEYS UITPRINTEN
//            System.out.println("Alle keys van de incubatietijd");
//            for (String s : matchingService.mappingDayNyms.keySet()) {
//                System.out.println("-------------------------------------------------------------------------");
//                System.out.println("Key: " + s);
//                for (String hash: matchingService.mappingDayNyms.get(s)){
//                    System.out.println("Value: " + hash);
//                }
//            }

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
        int incubatieTijd = 7;
       ListMultimap<String, String> temp = registrarInterface.getMappingDayNyms(incubatieTijd);
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
        boolean matchGevonden = false;
        //HIER NOG CHECKEN OF ER GEEN DUPLICATE IS
        for(Capsule nieuweCapsule: nieuweCapsules){
            for (Capsule currentCapsule: capsulesDB){
                if (currentCapsule.getTokenCustomer().getSignature().equals(nieuweCapsule.getTokenCustomer().getSignature())){
                    System.out.println("Dit is de huidige capsule voor: " + currentCapsule);
                    currentCapsule.setGeinformeerd(true);
                    matchGevonden = true;
                    System.out.println("Dit is de huidige capsule na: " + currentCapsule);
                }
            }
            if (!matchGevonden){
                capsulesDB.add(nieuweCapsule);
            }
        }
    }


    //deze methode is er om vanuit de customer te checken of je in contact bent gekomen met een besmet persoon
    @Override
    public boolean requestInfectedOrNot(List<Bezoek> bezoekenLaatsteZevenDagen) throws RemoteException {

        for (Bezoek bezoek : bezoekenLaatsteZevenDagen){
            for (Capsule capsule : infectedCapsules){
                //if (capsule.isErOverlap(bezoek)){
                    //return true;
                //}
            }
        }

        return false;
    }

    @Override
    public void setInfectedCapsules(List<Bezoek> bezoekenPatient) throws RemoteException {

        for (Bezoek bezoek : bezoekenPatient){
            for (Capsule capsule : capsulesDB){
                if (capsule.getHashBar() == bezoek.getCapsule().getHashBar()){
                    capsule.setInfected(true);
                    infectedCapsules.add(capsule);
                }
            }
        }

    }

    public void setInformed(List<Bezoek> bezoekenPatient) throws RemoteException {
        for (Bezoek bezoek : bezoekenPatient){
            for (Capsule capsule : capsulesDB){
                if (capsule.getTokenCustomer().getSignature().equals(bezoek.getCapsule().getTokenCustomer().getSignature())){
                    capsule.setGeinformeerd(true);
                    criticalTuples.add(new CriticalTuple(capsule.getHashBar(),capsule.getTimestampEntered(),capsule.getTimestampLeaving()));
                    System.out.println("Dit was de capsule: " + capsule);
                    System.out.println("Nieuwe tuple toegevoegd: " + criticalTuples.get(0));
                }
            }
        }

    }
    //TODO:
    //Wat er gebeurt:
    //        1) Docter stuurt de Bezoeken van de COVID patient door
    //        2) Matching ontvangt die en gaat alle capsules die gelijk zijn aan de bezoeken (dus alle parameters) op "geinformeerd zetten" (logisch want patient weet ondertussen datem covid heeft. De andere worden op "niet geinformeerd gezet"
    //        3) Matching maakt een Critical Tuple aan met daarin (hashBar + interval. Die critical tuple wil eigenlijk gewoon zeggen ja rond dat uur zat er een COVID in uw Bar
    //        4) De andere Customers drukken ton keer op de COVID knop en halen die critical tuples op van de matching, ze kijken of er in hun lokale database een bezoek zit die overeenkomt met de gegevens van de critial tuple.
    //        5) Als ze zien daze een match hebben maken ze een nieuwe Capsule aan met dezelfde token als dien dag, mixing flusht dat ton, Matching checkt of die capsule niet al in zijn DB zit --> Zo ja dan zet die de capsule op "geinformeerd"
    //        6) Wanneer er op het einde van de dag nog capsules zijn die op "niet geinformeerd" staan dan worden die naar de registrar gestuurd. De registrar weet namelijk van wie de tokens zijn en gaat de mapping van tokens overlopen en ze informeren via berichtje ofz

    @Override
    public void receiveInfectedBezoeken(List<Bezoek> infectedBezoeken) throws RemoteException {
        //STAP 1: ALLE CAPSULES DIE OVEREENKOMEN MET DE BEZOEKEN, KORTOM DE USER ZIJN EIGEN CAPSULES GAAN ZOEKEN, EN DE INFORMED TAG OP TRUE ZETTEN --> DE OVERIGE WORDEN OP FALSE GEZET OF BLIJVEN STAAN
        // DEZE ACTIE STAAT DUS GEWOON GELIJK AAN HET AL OP GEINFROMEERD ZETTEN VAN DE USER ZIJN EIGEN TOKENS
        //STAP 2: AANMAKEN VAN EEN CRITICAL TUPLE, DEZE TUPELS GAAN OPSLAAN TOT EEN USER ZE OPVRAAGD
        System.out.println("De aptient bracht zoveel bezoeken"+infectedBezoeken.size());
        setInformed(infectedBezoeken);

        //STAP 3: MATCHING IS KLAAR MET ZIJN WERK EN WACHT TOT USER EEN GETCRITICALTUPLE REQUEST DOEN, ZIE IN HIERONDER
    }

    @Override
    public List<CriticalTuple> requestCriticalTuples() throws RemoteException {
        return criticalTuples;
    }


    //------------------------------------------------------------------------------------------------------------------------------------------//

}
