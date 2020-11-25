package registrar;

import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface RegistrarInterface extends Remote{

    void requestDailyCustomerToken(int phoneNumber) throws RemoteException;

    List requestMonthlyHash(int bussinesNumber) throws RemoteException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException;
}
