package registrar;

import javax.crypto.spec.SecretKeySpec;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegistrarInterface extends Remote{

    void requestDailyCustomerToken(int phoneNumber) throws RemoteException;

    SecretKeySpec[] requestMonthlyKeys(int bussinesNumber) throws RemoteException;
}
