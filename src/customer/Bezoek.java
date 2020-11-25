package customer;

import java.sql.Timestamp;
import java.util.Date;

public class Bezoek {
    private String timestamp;
    private String randomIntBar;
    private String businessNumberBar;
    private String hashBar;

    public String getTimestamp() {
        return timestamp;
    }

    public String getHashBar() {
        return hashBar;
    }

    public Bezoek(){}

    public Bezoek(String randomIntBar, String businessNumberBar, String hashBar) {
        this.randomIntBar = randomIntBar;
        this.businessNumberBar = businessNumberBar;
        this.hashBar = hashBar;

        //Date object
        Date date= new Date();
        //getTime() returns current time in milliseconds
        long time = date.getTime();
        //Passed the milliseconds to constructor of Timestamp class
        Timestamp ts = new Timestamp(time);
        this.timestamp = ts.toString();
    }
}
