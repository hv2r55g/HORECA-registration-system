package customer;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Bezoek {
    private long timestampEntered;
    private String timestampEnteredString;
    private long timestampLeaving;
    private String timestampLeavingString;
    private String randomIntBar;
    private String businessNumberBar;
    private byte[] hashBar;
    private boolean infected;

    public long getTimestampEntered() {
        return timestampEntered;
    }

    public long getTimestampLeaving() {
        return timestampLeaving;
    }

    public String getRandomIntBar() {
        return randomIntBar;
    }

    public String getBusinessNumberBar() {
        return businessNumberBar;
    }

    public byte[] getHashBar() {
        return hashBar;
    }

    public String getTimestampEnteredString() {
        return new SimpleDateFormat("ddMMMMyyyy HH:mm").format(new Date(timestampEntered));
    }


    public String getTimestampLeavingString() {
        return new SimpleDateFormat("ddMMMMyyyy HH:mm").format(new Date(timestampLeaving));
    }


    public Bezoek(){}

    public void setTimestampEntered(long timestampEntered) {
        this.timestampEntered = timestampEntered;
    }

    public void setTimestampEnteredString(String timestampEnteredString) {
        this.timestampEnteredString = timestampEnteredString;
    }

    public void setTimestampLeaving(long timestampLeaving) {
        this.timestampLeaving = timestampLeaving;
    }

    public void setTimestampLeavingString(String timestampLeavingString) {
        this.timestampLeavingString = timestampLeavingString;
    }

    public void setRandomIntBar(String randomIntBar) {
        this.randomIntBar = randomIntBar;
    }

    public void setBusinessNumberBar(String businessNumberBar) {
        this.businessNumberBar = businessNumberBar;
    }

    public void setHashBar(byte[] hashBar) {
        this.hashBar = hashBar;
    }

    public boolean isInfected() {
        return infected;
    }

    public void setInfected(boolean infected) {
        this.infected = infected;
    }

    public Bezoek(long timestampEntered, long timestampLeaving, String randomIntBar, String businessNumberBar, byte[] hashBar) {
        this.timestampEntered = timestampEntered;
        this.timestampLeaving = timestampLeaving;
        this.randomIntBar = randomIntBar;
        this.businessNumberBar = businessNumberBar;
        this.hashBar = hashBar;
        this.infected = false;
    }

    @Override
    public String toString() {
        return "Bezoek{" +
                "timestampEntered=" + timestampEntered +
                ", timestampEnteredString='" + timestampEnteredString + '\'' +
                ", timestampLeaving=" + timestampLeaving +
                ", timestampLeavingString='" + timestampLeavingString + '\'' +
                ", randomIntBar='" + randomIntBar + '\'' +
                ", businessNumberBar='" + businessNumberBar + '\'' +
                ", hashBar=" + Arrays.toString(hashBar) +
                '}';
    }
}
