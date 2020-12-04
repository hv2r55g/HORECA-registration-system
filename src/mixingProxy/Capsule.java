package mixingProxy;

import registrar.Token;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;

public class Capsule implements Serializable{
    private static final long serialVersionUID = 20120731125400L;

    private long timestampEntered;
    private long timestampLeaving;
    private Token tokenCustomer;
    private String hashBar;
    private String dagBezoek;
    private boolean infected;

    public long getTimestampEntered() {
        return timestampEntered;
    }

    public void setTimestampEntered(long timestampEntered) {
        this.timestampEntered = timestampEntered;
    }

    public long getTimestampLeaving() {
        return timestampLeaving;
    }

    public void setTimestampLeaving(long timestampLeaving) {
        this.timestampLeaving = timestampLeaving;
    }

    public Token getTokenCustomer() {
        return tokenCustomer;
    }

    public void setTokenCustomer(Token tokenCustomer) {
        this.tokenCustomer = tokenCustomer;
    }

    public String getHashBar() {
        return hashBar;
    }

    public void setHashBar(String hashBar) {
        this.hashBar = hashBar;
    }

    public String getDagBezoek() {
        return dagBezoek;
    }

    public void setDagBezoek(String dagBezoek) {
        this.dagBezoek = dagBezoek;
    }

    public boolean isInfected() {
        return infected;
    }

    public void setInfected(boolean infected) {
        this.infected = infected;
    }

    public Capsule(){}

    public Capsule(Token tokenCustomer, String hashBar) {
        this.tokenCustomer = tokenCustomer;
        this.hashBar = hashBar;
        this.timestampEntered = -1;
        this.timestampLeaving = -1;
        this.infected = false;
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
                ", hashBar='" + hashBar + '\'' +
                ", dagBezoek='" + dagBezoek + '\'' +
                ", infected='" + infected + '\'' +
                '}';
    }
}
