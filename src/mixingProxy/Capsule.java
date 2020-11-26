package mixingProxy;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Arrays;

public class Capsule implements Serializable{
    private static final long serialVersionUID = 20120731125400L;

    private String timestampBezoek;
    private byte[] tokenCustomer;
    private String hashBar;

    public Capsule(){}

    public Capsule(String timestampBezoek, byte[] tokenCustomer, String hashBar) {
        this.timestampBezoek = timestampBezoek;
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
