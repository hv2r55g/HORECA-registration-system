package customer;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Bezoek {
    private long timestamp;
    private String randomIntBar;
    private String businessNumberBar;
    private String hashBar;

    public long getTimestamp() {
        return timestamp;
    }

    public String getHashBar() {
        return hashBar;
    }

    public Bezoek(){}

    public Bezoek(long timestamp,String randomIntBar, String businessNumberBar, String hashBar) {
        this.timestamp = timestamp;
        this.randomIntBar = randomIntBar;
        this.businessNumberBar = businessNumberBar;
        this.hashBar = hashBar;
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
