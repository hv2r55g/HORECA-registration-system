package mixingProxy;

import customer.Bezoek;

import doctor.CriticalTuple;
import registrar.Token;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;

public class Capsule implements Serializable{
    private static final long serialVersionUID = 20120731125400L;

    private long timestampEntered;
    private long timestampLeaving;
    private String timestampEnteredString;
    private String timestampLeavingString;
    private Token tokenCustomer;
    private String hashBar;
    private String dagBezoek;
    private boolean infected;
    private boolean geinformeerd;
    //VOOR GUI
    private String tokenSign;
    private String tokenData;

    public Capsule(long te, long tl, Token token, String hashBar) {
        this.timestampEntered = te;
        this.timestampLeaving = tl;
        this.tokenCustomer = token;
        this.hashBar = hashBar;
    }

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

    public String getTimestampEnteredString() {
        return new SimpleDateFormat("ddMMMMyyyy HH:mm").format(new Date(timestampEntered));
    }

    public String getTimestampLeavingString() {
        return new SimpleDateFormat("ddMMMMyyyy HH:mm").format(new Date(timestampLeaving));
    }

    public String getTokenSign() {
        return tokenSign;
    }

    public String getTokenData() {
        return tokenData;
    }
    
    

    public boolean isGeinformeerd() {
        return geinformeerd;
    }

    public void setGeinformeerd(boolean geinformeerd) {
        this.geinformeerd = geinformeerd;
    }

    public Capsule(){}

    public Capsule(Token tokenCustomer, String hashBar) {
        this.tokenCustomer = tokenCustomer;
        this.tokenSign = tokenCustomer.getSignature();
        this.tokenData = tokenCustomer.getDatumInfo();
        this.hashBar = hashBar;
        this.timestampEntered = -1;
        this.timestampLeaving = -1;
        this.infected = false;
        this.geinformeerd = false;
    }

    public boolean isErOverlap(CriticalTuple criticalTuple){
        //BEREKEN OF ER OVERLAP IS WAARBIJ
        if (timestampLeaving > criticalTuple.getTimeEntered() && criticalTuple.getTimeLeft() > timestampEntered){
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Capsule{" +
                "timestampEntered=" + timestampEntered +
                ", timestampLeaving=" + timestampLeaving +
                ", timestampEnteredString='" + timestampEnteredString + '\'' +
                ", timestampLeavingString='" + timestampLeavingString + '\'' +
                ", tokenCustomer=" + tokenCustomer +
                ", hashBar='" + hashBar + '\'' +
                ", dagBezoek='" + dagBezoek + '\'' +
                ", infected=" + infected +
                ", geinformeerd=" + geinformeerd +
                ", tokenSign='" + tokenSign + '\'' +
                ", tokenData='" + tokenData + '\'' +
                '}';
    }
}
