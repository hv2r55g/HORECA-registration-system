package doctor;

import customer.Bezoek;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Doctor {
    private String currentPatient;
    private List<Bezoek> bezoekenPatient;

    public Doctor(){}

    public Doctor(String patient) {
        this.currentPatient = patient;
        this.bezoekenPatient = new ArrayList<>();
    }

    public static void main(String[] args) {
        System.out.println("Welkom dokter");
        //System.out.println("Wie bent u?");
        //Scanner sc = new Scanner(System.in);
        //String telefoonNrPatient = sc.nextLine();
        String patient = "123456789";
        Doctor currentDoctor = new Doctor(patient);

        //PATIENT HEEFT COVID, LEES BEZOEKEN IN
        currentDoctor.leesBezoekenPatientIn();

    }

    private void leesBezoekenPatientIn() {
        String path = "src/DoktersBestanden/";
        File currentFile = new File(path+ currentPatient +".csv");
        try {
            Scanner sc = new Scanner(currentFile);
            String firstLine = sc.nextLine();
            System.out.println(firstLine);
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
