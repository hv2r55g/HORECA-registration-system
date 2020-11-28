package mixingProxy;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;

public class Capsule implements Serializable{
    private static final long serialVersionUID = 20120731125400L;

    private String timestampBezoek;
    private byte[] tokenCustomer;
    private String hashBar;
    private String dagBezoek;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getTimestampBezoek() {
        return timestampBezoek;
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

    public Capsule(){}

    public Capsule(String timestampBezoek, String dag, byte[] tokenCustomer, String hashBar) {
        this.timestampBezoek = timestampBezoek;
        this.dagBezoek = dag;
        this.tokenCustomer = tokenCustomer;
        this.hashBar = hashBar;
    }

    @Override
    public String toString() {
        return "Capsule{" +
                "timestampBezoek='" + timestampBezoek + '\'' +
                ", tokenCustomer=" + Arrays.toString(tokenCustomer) +
                ", hashBar='" + hashBar + '\'' +
                '}';
    }
}
