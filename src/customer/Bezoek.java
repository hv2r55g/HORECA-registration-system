package customer;

import mixingProxy.Capsule;
import registrar.Token;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Bezoek implements Serializable {
    private static final long serialVersionUID = 20120732225400L;

    private String randomIntBar;
    private String businessNumberBar;
    private Capsule capsule;
    private String tokenSign;
    private String timestampEnteredString;
    private String timestampLeavingString;
    private String hashBar;

    public Bezoek(long TE, long TL, String tokenSign, String randomIntBar, String businessNumberBar, String hashBar) {
        this.randomIntBar = randomIntBar;
        this.businessNumberBar = businessNumberBar;
        this.capsule = new Capsule(TE,TL,new Token(tokenSign),hashBar);
        //this.tokenSign = capsule.getTokenSign();
        this.tokenSign = capsule.getTokenCustomer().getSignature();
        this.hashBar = capsule.getHashBar();
    }

    public void setRandomIntBar(String randomIntBar) {
        this.randomIntBar = randomIntBar;
    }

    public void setBusinessNumberBar(String businessNumberBar) {
        this.businessNumberBar = businessNumberBar;
    }

    public Capsule getCapsule() {
        return capsule;
    }

    public void setCapsule(Capsule capsule) {
        this.capsule = capsule;
    }

    public String getRandomIntBar() {
        return randomIntBar;
    }

    public String getBusinessNumberBar() {
        return businessNumberBar;
    }

    public String getTokenSign() {
        return tokenSign;
    }

    public String getTimestampEnteredString() {
        return new SimpleDateFormat("ddMMMMyyyy HH:mm").format(new Date(capsule.getTimestampEntered()));
    }


    public String getTimestampLeavingString() {
        return new SimpleDateFormat("ddMMMMyyyy HH:mm").format(new Date(capsule.getTimestampLeaving()));
    }

    public String getHashBar() {
        return hashBar;
    }

    public Bezoek(){}
    
    public Bezoek(String randomIntBar, String businessNumberBar,Capsule capsule) {
        this.randomIntBar = randomIntBar;
        this.businessNumberBar = businessNumberBar;
        this.capsule = capsule;
        this.tokenSign = capsule.getTokenSign();
        this.hashBar = capsule.getHashBar();
    }

    @Override
    public String toString() {
        return "Bezoek{" +
                "randomIntBar='" + randomIntBar + '\'' +
                ", businessNumberBar='" + businessNumberBar + '\'' +
                ", capsule=" + capsule +
                ", tokenSign='" + tokenSign + '\'' +
                ", timestampEnteredString='" + timestampEnteredString + '\'' +
                ", timestampLeavingString='" + timestampLeavingString + '\'' +
                ", hashBar='" + hashBar + '\'' +
                '}';
    }
}
