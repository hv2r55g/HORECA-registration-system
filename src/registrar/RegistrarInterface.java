package registrar;

import javax.crypto.spec.SecretKeySpec;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegistrarInterface extends Remote{

    void getDailySecretKey(int businessNumber) throws RemoteException;

    void generateDailyCustomerToken(int phoneNumber) throws RemoteException;

    void requestMonthlyKeys(int bussinesNumber) throws RemoteException;
}
