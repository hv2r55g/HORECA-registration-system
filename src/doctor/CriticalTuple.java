package doctor;

import java.io.Serializable;

public class CriticalTuple implements Serializable {
    private static final long serialVersionUID = 20120733325400L;
    private String hashBar;
    private long timeEntered;
    private long timeLeft;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getHashBar() {
        return hashBar;
    }

    public long getTimeEntered() {
        return timeEntered;
    }

    public long getTimeLeft() {
        return timeLeft;
    }
    
    public CriticalTuple(){}

    public CriticalTuple(String hashBar, long timeEntered, long timeLeft) {
        this.hashBar = hashBar;
        this.timeEntered = timeEntered;
        this.timeLeft = timeLeft;
    }

    @Override
    public String toString() {
        return "CriticalTuple{" +
                "hashBar='" + hashBar + '\'' +
                ", timeEntered=" + timeEntered +
                ", timeLeft=" + timeLeft +
                '}';
    }
}
