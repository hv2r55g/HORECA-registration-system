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
    private String hashBar;

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

    public String getHashBar() {
        return hashBar;
    }

    public String getTimestampEnteredString() {
        return new SimpleDateFormat("ddMMMMyyyy HH:mm").format(new Date(timestampEntered));
    }


    public String getTimestampLeavingString() {
        return new SimpleDateFormat("ddMMMMyyyy HH:mm").format(new Date(timestampLeaving));
    }


    public Bezoek(){}

    public Bezoek(long timestampEntered,long timestampLeaving,String randomIntBar, String businessNumberBar, String hashBar) {
        this.timestampEntered = timestampEntered;
        this.timestampLeaving = timestampLeaving;
        this.randomIntBar = randomIntBar;
        this.businessNumberBar = businessNumberBar;
        this.hashBar = hashBar;
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
                ", hashBar=" + hashBar +
                '}';
    }
}
