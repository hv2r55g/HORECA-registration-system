import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class CryptoTest {

    public static boolean isValid = false;
    public static MessageDigest digester;
    public static void main(String[] args) {

        //HET EERSTE GEDEELTE IS VOOR BAR
        try {
            digester = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        SecureRandom random = new SecureRandom();

        ArrayList<String> pseudonyms = new ArrayList<>();
        pseudonyms.add("test");
        pseudonyms.add("root");
        Map<String, String> entries = new HashMap<>();

        // hashing -> encypting
        for (String p : pseudonyms) {
            // Ri
            byte[] pseudonymBytes = p.getBytes();
            byte[] ri = new byte[20];
            byte[] hash = null;
            random.nextBytes(ri);

            // H(Ri, nym_CF_day_i)
            ByteOutputStream bos = new ByteOutputStream();
            byte[] output;

            try (DataOutputStream dos = new DataOutputStream(bos)) {
                dos.write(ri);
                dos.write(pseudonymBytes);
                dos.flush();
                hash = digester.digest(bos.getBytes());

                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            entries.put(Base64.getEncoder().encodeToString(ri), Base64.getEncoder().encodeToString(hash));
            System.out.println("pseudonym: " + p + ", ri: " + Base64.getEncoder().encodeToString(ri)+ ", hash: " + Base64.getEncoder().encodeToString(hash));
        }

        //DIT ZAL VOOR MATCHING ZIJN!!!!
        // hashing -> check is valid
        entries.forEach((key, value) -> {
            isValid = false;
            for (String p : pseudonyms) {
                byte[] pseudonym = p.getBytes();
                byte[] ri = Base64.getDecoder().decode(key.getBytes());
                byte[] hash = null;

                ByteOutputStream bos = new ByteOutputStream();
                byte[] output;

                try {
                    try (DataOutputStream dos = new DataOutputStream(bos)) {
                        dos.write(ri);
                        dos.write(pseudonym);
                        dos.flush();
                        hash = digester.digest(bos.getBytes());
                        bos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String hashValue = Base64.getEncoder().encodeToString(hash);
                if (hashValue.equals(value)) {
                    isValid = true;
                }
                System.out.println("Check hash: " + value + " = " + hashValue);
            }
        });
        if (!isValid){
            System.out.println("Could not find a valid hash");
        } else {
            System.out.println("Found the valid hashes");
        }

    }

    //RANDOM STUK CODE. NIET VAN BELANG VOOR BOVENSTAANDE!!!!
    public String generatePseudonym(long businessNumber, String location, LocalDate day) throws Exception {
        // generate each day_i a new pseudonym nym_CF_day_i
        // H is a cryptographic hashing function
        // nym_CF_day_i = H(sKeyCF, locationCF, day_i)

        MessageDigest digester = MessageDigest.getInstance("SHA-256");
        byte[] secretKey = generateSecretKey(businessNumber, day);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        dos.write(location.getBytes());
        dos.write(secretKey);
        dos.write(day.toString().getBytes());
        dos.flush();

        byte[] output = digester.digest(bos.toByteArray());

        dos.close();
        bos.close();

        String outputString = Base64.getEncoder().encodeToString(output);

        return outputString;

    }

    private byte[] generateSecretKey(long businessNumber, LocalDate day) {
        return new byte[0];
    }
}