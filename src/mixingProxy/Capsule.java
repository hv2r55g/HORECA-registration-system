package mixingProxy;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;

public class Capsule implements Serializable{
    private static final long serialVersionUID = 20120731125400L;

    private long timestampEntered;
    private long timestampLeaving;
    private byte[] tokenCustomer;
    private String hashBar;
    private String dagBezoek;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public byte[] getTokenCustomer() {
        return tokenCustomer;
    }

    public String getHashBar() {
        return hashBar;
    }

    public String getDagBezoek() {
        return dagBezoek;
    }

    public long getTimestampEntered() { return timestampEntered; }

    public long getTimestampLeaving() { return timestampLeaving; }

    public void setDagBezoek(String dagBezoek) { this.dagBezoek = dagBezoek; }

    public void setTimestampEntered(long timestampEntered) { this.timestampEntered = timestampEntered; }

    public void setTimestampLeaving(long timestampLeaving) {
        this.timestampLeaving = timestampLeaving;
    }

    public Capsule(){}

    public Capsule(byte[] tokenCustomer, String hashBar) {
        this.tokenCustomer = tokenCustomer;
        this.hashBar = hashBar;
    }

    public boolean isErOverlap(Capsule infected){
        //BEREKEN OF ER OVERLAP IS WAARBIJ
        if (timestampLeaving > infected.getTimestampEntered() && infected.getTimestampLeaving() > timestampEntered){
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Capsule{" +
                "timestampEntered=" + timestampEntered +
                ", timestampLeaving=" + timestampLeaving +
                ", tokenCustomer=" + Arrays.toString(tokenCustomer) +
                ", hashBar='" + hashBar + '\'' +
                ", dagBezoek='" + dagBezoek + '\'' +
                '}';
    }
}
