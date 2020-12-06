package registrar;

import java.io.Serializable;

public class Token implements Serializable {
    private static final long serialVersionUID = 20120732225400L;
    private String signature;
    private String datumInfo;

    public Token(String tokenSign) {
        this.signature = tokenSign;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getDatumInfo() {
        return datumInfo;
    }

    public void setDatumInfo(String datumInfo) {
        this.datumInfo = datumInfo;
    }

    public Token(String signature, String datumInfo) {
        this.signature = signature;
        this.datumInfo = datumInfo;
    }
}
