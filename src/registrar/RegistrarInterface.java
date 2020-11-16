package registrar;

import bar.Bar;
import bar.BarInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RemoteRef;

public interface RegistrarInterface extends Remote{
    void getDailySecretKey(int businessNumber) throws RemoteException;
    
    int getMasterSecretKey() throws RemoteException;

}
