package registrar;

import java.io.UnsupportedEncodingException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.List;

public interface RegistrarInterface extends Remote{

    List requestMonthlyHash(int bussinesNumber) throws RemoteException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException;

    List requestDailyCustomerToken(String phoneNumber) throws RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException;
}
