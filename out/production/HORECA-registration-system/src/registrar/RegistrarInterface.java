package registrar;

import bar.Bar;
import bar.BarInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RemoteRef;

public interface RegistrarInterface extends Remote{
    void sendMessageToRegistrar(String message, BarInterface bar) throws RemoteException;

}
