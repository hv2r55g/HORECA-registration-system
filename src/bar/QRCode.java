package bar;

import java.io.Serializable;
import java.util.Arrays;

public class QRCode implements Serializable {
    private static final long serialVersionUID = 20120732125400L;
    private String randomGetal;
    private String businessNumber;
    private String hashBar;

    public QRCode() {
    }

    public QRCode(String randomGetal, String businessNumber, String hashBar) {
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

    public String getHashBar() {
        return hashBar;
    }

    public void setHashBar(String hashBar) {
        this.hashBar = hashBar;
    }

    @Override
    public String toString() {
        return "QRCode{" +
                "randomGetal='" + randomGetal + '\'' +
                ", businessNumber='" + businessNumber + '\'' +
                ", hashBar=" + hashBar +
                '}';
    }
}
