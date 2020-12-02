package customer;


import mixingProxy.Capsule;
import mixingProxy.MixingProxyInterface;
import registrar.RegistrarInterface;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public class Customer extends UnicastRemoteObject implements CustomerInterface {
    private String phoneNumber;
    private List<byte[]> tokens;
    private byte[] currentToken;
    private List<Bezoek> bezoeken;
    private String[] QRcodeCurrentBar;
    private Map<String,String> mappingIcons;
    private RegistrarInterface registrarInterface;
    private MixingProxyInterface mixingProxyInterface;


    public Customer() throws RemoteException {}

    public Customer(String phoneNumber) throws RemoteException {
        this.phoneNumber = phoneNumber;
        this.bezoeken = new ArrayList();
        this.QRcodeCurrentBar = new String[3];
        this.mappingIcons = new HashMap<>();
    }

    private void requestTokens() throws RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        tokens = registrarInterface.requestDailyCustomerToken(phoneNumber);
    }

    private void bezoekBar() throws RemoteException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        if (!tokens.isEmpty()) {
            //CAPSULE OPMAKEN
            currentToken = tokens.get(0);
            Capsule capsule = new Capsule(currentToken, QRcodeCurrentBar[2]);

            //sendCapsule is een boolean geworden die de checks gaat uitvoeren en true geeft als het gelukt is
            //wnr true gaat de mixing het opslaan en het signen
            //wnr true vragen we de sign op!!!
            boolean doSign = mixingProxyInterface.sendCapsule(capsule);
            if (doSign) {
                //dan moet men de sign ontvangen
                byte[] signedCapsule = mixingProxyInterface.signCapsule(capsule);
                System.out.println("Dit is de bytearray van de sign: " + Arrays.toString(signedCapsule));

                //KIJKEN WELK LOGO DIE MOET KRIJGEN
                //BEETJE INT MIDDEN ANDERS STEEDS ZELFDE GETAL
                showLogo(signedCapsule[10]);
            } else {
                System.out.println("bezoek gefailed, waarschijnlijk door een check");
            }
            //GEBRUIKT TOKEN VERWIJDEREN
            tokens.remove(0);
        } else {
            System.out.println("U kan deze bar helaas niet meer bezoeken, uw tokens voor vandaag zijn verbruikt");
        }
    }

    private void showLogo(int signedCapsule) {
        int eersteGetal = Math.abs(signedCapsule);
        for(String currentInterval: mappingIcons.keySet()){
            String[] interval = currentInterval.split("-");
            if ((Integer.parseInt(interval[0])<= eersteGetal) && (eersteGetal<= Integer.parseInt(interval[1]))){
                System.out.println("Het getal bevindt zich in het interval " + currentInterval + " en krijgt de " + mappingIcons.get(currentInterval) + " toegewezen");
                break;
            } else if (100 <= eersteGetal){
                System.out.println("Het getal is groter dan 100, en krijgt de default waarde mee");
                break;
            } else if (0 > eersteGetal){
                System.out.println("Er is iets misgelopen met uw ABS");
            }
        }
    }

    private void verlaatBar() throws RemoteException {
        //LEAVING TIME IN CAPSULE GAAN FIXEN, TERGELIJK NOG KEER DAT OBECT TERUGSTUREN VOOR ONS BEZOEK AAN TE MAKEN
        Capsule currentCapsule = mixingProxyInterface.requestLeaving(currentToken);
        //BEZOEK LOKAAL OPSLAAN
        Bezoek bezoek = new Bezoek(currentCapsule.getTimestampEntered(),currentCapsule.getTimestampLeaving(),QRcodeCurrentBar[0],QRcodeCurrentBar[1],QRcodeCurrentBar[2]);
        bezoeken.add(bezoek);   //hoeft niet mer perse
        sendToLocalDatabase(bezoek);
    }

    private void sendToLocalDatabase(Bezoek bezoek){
        try {
            System.out.println("Voeg bezoek toe aan local database");
            String path = "src/DoktersBestanden/";
            String fileName = phoneNumber + ".csv";
            File file = new File(path+fileName);

            FileWriter fileWritter = new FileWriter(path+fileName,true);
            BufferedWriter bw = new BufferedWriter(fileWritter);
            Scanner sc = new Scanner(file);

            if(!sc.hasNextLine()) {
                System.out.println("File bestaat nog niet");
                bw.append("Timestamp entering;Timestamp leaving;Random number bar;Bussiness number bar;Hash bar");
                bw.newLine();
                bw.append(bezoek.getTimestampEntered() + ";" + bezoek.getTimestampLeaving()+ ";" + bezoek.getRandomIntBar()+ ";" + bezoek.getBusinessNumberBar()+ ";" + bezoek.getHashBar());
                bw.newLine();
            } else {
                System.out.println("File bestaat wel al");
                bw.append(bezoek.getTimestampEntered() + ";" + bezoek.getTimestampLeaving()+ ";" + bezoek.getRandomIntBar()+ ";" + bezoek.getBusinessNumberBar()+ ";" + bezoek.getHashBar());
                bw.newLine();
            }
            sc.close();
            bw.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    private void clearLocalDatabase() throws IOException {
        System.out.println("Clear local database");
        String path = "src/DoktersBestanden/";
        String fileName = phoneNumber + ".csv";

        FileWriter fileWritter = new FileWriter(path+fileName);
        BufferedWriter bw = new BufferedWriter(fileWritter);
        bw.write("");
        bw.close();
    }

    public static void main(String[] args) throws IOException, NotBoundException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidKeySpecException {
        //Scanner sc = new Scanner(System.in);
        //System.out.println("Geef uw gsm nummer: ");
        //int phoneNumber = sc.nextInt();
        String phoneNumber = "00000003";
        Customer currentCustomer = new Customer(phoneNumber);

        //1 KEER PER DAG BEIDE LIJSTEN SHUFFELEN? KWEET ALLEEN NIET GOED HOE IK HET MOET IMPLEMENTEREN
        //VOORLOPIG GEWOON GELIJKE INDEXEN GELIJK STELLEN AAN ELKAAR
        String[] interval = {"0-4","5-9","10-14","15-19","20-24","25-29","30-34","35-39","40-44","45-49","50-54","55-59","60-64","65-69","70-74","75-79","80-84","85-89","90-94","95-99",};
        String[] afbeeldingen = {"BlueDot.jpg","BlueSquare.jpg","BlueStar","BlueTriangle.jpg","BlueHeart.jpg",
                                    "YellowDot.jpg","YellowSquare.jpg","YellowStar","YellowTriangle.jpg", "YellowHeart.jpg",
                                    "GreenDot.jpg","GreenSquare.jpg","GreenStar","GreenTriangle.jpg","GreenHeart.jpg",
                                    "RedDot.jpg","RedSquare.jpg","RedStar","RedTriangle.jpg","RedHeart.jpg"};
        //SHUFFELEN
        List<String> intervalArray = Arrays.asList(interval);
        List<String> afbeeldingenArray = Arrays.asList(afbeeldingen);
        //Collections.shuffle(intervalArray);
        //Collections.shuffle(afbeeldingenArray);

        boolean evenGroot = interval.length == afbeeldingen.length;
        System.out.println("Zijn de sizes even groot? " + evenGroot);
        if (evenGroot){
            for (int i = 0; i < interval.length; i++) {
                currentCustomer.mappingIcons.put(intervalArray.get(i),afbeeldingenArray.get(i));
            }
        }





        //CONNECTEN MET REGISTRAR
        String hostname = "localhost";
        String clientService = "RegistrarListening";
        String servicename = "RegistrarService";
        Naming.rebind("rmi://" + hostname + "/" + clientService, currentCustomer);
        RegistrarInterface registrarInterface = (RegistrarInterface) Naming.lookup("rmi://" + hostname + "/" + servicename);
        currentCustomer.registrarInterface = registrarInterface;

        //CONNECTEN MET MIXING PROXY
        clientService = "MixingProxyListening";
        servicename = "MixingProxyService";
        Naming.rebind("rmi://" + hostname + "/" + clientService, currentCustomer);
        MixingProxyInterface mixingProxyInterface = (MixingProxyInterface) Naming.lookup("rmi://" + hostname + "/" + servicename);
        currentCustomer.mixingProxyInterface = mixingProxyInterface;

        //GUI KAN GESTART WORDEN MET ONDERSTAANDE LIJN
        //CustomerGUI app = new CustomerGUI(args);

        //1 AANVRAAG (VAN 48 TOKENS) PER DAG, IEDERE TOKEN KAN MAAR 1 KEER GEBRUIKT WORDEN
        currentCustomer.requestTokens();

        //WE BEZOEKEN EEN BAR, TIJDELIJK PLAKKEN WE HIER ALLE QR CODE INFO
        String PLAKDESTRINGHIER = "8773;1;uWxv�(7�\u0018���H\u001B{ L�;";
        //KAN MISSCHIEN IN METHODKE VO CLEANER
        String[] temp = PLAKDESTRINGHIER.split(";");
        currentCustomer.QRcodeCurrentBar[0] = temp[0];
        currentCustomer.QRcodeCurrentBar[1] = temp[1];
        currentCustomer.QRcodeCurrentBar[2] = temp[2];


        //SIMULATIE BEZOEKEN
        currentCustomer.bezoekBar();
        currentCustomer.verlaatBar();
        //currentCustomer.bezoekBar();
        //currentCustomer.verlaatBar();

        //METHODE OM KEER HEEL DIE FILE TE CLEAREN
        //currentCustomer.clearLocalDatabase();


    }
}
