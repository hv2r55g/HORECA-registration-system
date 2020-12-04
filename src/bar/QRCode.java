package bar;

import java.util.Arrays;

public class QRCode {
    private String randomGetal;
    private String businessNumber;
    private byte[] hashBar;

    public QRCode() {
    }

    public QRCode(String randomGetal, String businessNumber, byte[] hashBar) {
        this.randomGetal = randomGetal;
        this.businessNumber = businessNumber;
        this.hashBar = hashBar;
    }

    public String getRandomGetal() {
        return randomGetal;
    }

    public void setRandomGetal(String randomGetal) {
        this.randomGetal = randomGetal;
    }

    public String getBusinessNumber() {
        return businessNumber;
    }

    public void setBusinessNumber(String businessNumber) {
        this.businessNumber = businessNumber;
    }

    public byte[] getHashBar() {
        return hashBar;
    }

    public void setHashBar(byte[] hashBar) {
        this.hashBar = hashBar;
    }

    @Override
    public String toString() {
        return "QRCode{" +
                "randomGetal='" + randomGetal + '\'' +
                ", businessNumber='" + businessNumber + '\'' +
                ", hashBar=" + Arrays.toString(hashBar) +
                '}';
    }
}
