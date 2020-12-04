package doctor;

import bar.Bar;
import customer.Bezoek;
import matchingService.MatchingServiceInterface;
import mixingProxy.MixingProxyInterface;
import registrar.RegistrarInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Doctor  extends UnicastRemoteObject implements Remote {
    private String currentPatient;
    private List<Bezoek> bezoekenPatient;
    private MatchingServiceInterface matchingServiceInterface;

    public Doctor() throws RemoteException {
        super();
    }

    public Doctor(String patient) throws RemoteException {
        super();
        this.currentPatient = patient;
        this.bezoekenPatient = new ArrayList<>();
    }

    public String getCurrentPatient() {
        return currentPatient;
    }

    public void setCurrentPatient(String currentPatient) {
        this.currentPatient = currentPatient;
    }

    public List<Bezoek> getBezoekenPatient() {
        return bezoekenPatient;
    }

    public void setBezoekenPatient(List<Bezoek> bezoekenPatient) {
        this.bezoekenPatient = bezoekenPatient;
    }

    public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException {


        Doctor currentDoctor = new Doctor();

        //Connecten met de registrar
        String hostname = "localhost";
        String clientService = "MatchingListening";
        String servicename = "MatchingService";
        Naming.rebind("rmi://" + hostname + "/" + clientService, currentDoctor);
        RegistrarInterface registrarInterface = (RegistrarInterface) Naming.lookup("rmi://" + hostname + "/" + servicename);
        currentDoctor.matchingServiceInterface = (MatchingServiceInterface) Naming.lookup("rmi://" + hostname + "/" + servicename);

        System.out.println("Welkom dokter");
        //System.out.println("Wie bent u?");
        //Scanner sc = new Scanner(System.in);
        //String telefoonNrPatient = sc.nextLine();
        String patient = "123456789";

        currentDoctor.setCurrentPatient(patient);

        //PATIENT HEEFT COVID, LEES BEZOEKEN IN
        currentDoctor.leesBezoekenPatientIn();

    }

    public void setInfected(List<Bezoek> bezoekenPatient){
        for (Bezoek bezoek : bezoekenPatient){
            bezoek.setInfected(true);
        }
    }

    public void setInfectedInMatching() throws RemoteException {
        matchingServiceInterface.setInfectedCapsules(bezoekenPatient);
    }

    private void leesBezoekenPatientIn() {
        String path = "src/DoktersBestanden/";
        File currentFile = new File(path+ currentPatient +".csv");
        try {
            Scanner sc = new Scanner(currentFile);
            String firstLine = sc.nextLine();
            while (sc.hasNextLine()){
                String currentBezoekString = sc.nextLine();
                String[] gegevens = currentBezoekString.split(";");
                Bezoek currentBezoek = new Bezoek(Long.parseLong(gegevens[0]),Long.parseLong(gegevens[1]),gegevens[2],gegevens[3],gegevens[4]);
                bezoekenPatient.add(currentBezoek);
            }
            System.out.println("Patient: " + currentPatient + " heeft de afgelopen tijd " + bezoekenPatient.size() + " keer een bezoek gebracht aan een bar of restaurant.");

        } catch (FileNotFoundException e) {
            System.out.println("De patient heeft nog geen dokterbestand aangemaakt");
        }
    }
}
