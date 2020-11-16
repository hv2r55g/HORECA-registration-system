package bar;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BarInterface extends Remote{
    void receiveMessage(String Message) throws RemoteException;
}
