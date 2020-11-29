package customer;

public class Bezoek {
    private long timestampEntered;
    private long timestampLeaving;
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
                "timestamp='" + timestampEntered + '\'' +
                ", randomIntBar='" + randomIntBar + '\'' +
                ", businessNumberBar='" + businessNumberBar + '\'' +
                ", hashBar='" + hashBar + '\'' +
                '}';
    }
}
