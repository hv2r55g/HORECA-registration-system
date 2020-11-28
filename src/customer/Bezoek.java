package customer;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Bezoek {
    private String timestamp;
    private String randomIntBar;
    private String businessNumberBar;
    private String hashBar;
    private String day;

    public String getTimestamp() {
        return timestamp;
    }

    public String getHashBar() {
        return hashBar;
    }

    public Bezoek(){}

    public String getDay() {
        return day;
    }

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

        SimpleDateFormat df  = new SimpleDateFormat("ddMMMMyyyy");
        String dagVanVandaag = df.format(date);
        this.day = dagVanVandaag;
    }

    @Override
    public String toString() {
        return "Bezoek{" +
                "timestamp='" + timestamp + '\'' +
                ", randomIntBar='" + randomIntBar + '\'' +
                ", businessNumberBar='" + businessNumberBar + '\'' +
                ", hashBar='" + hashBar + '\'' +
                '}';
    }
}
