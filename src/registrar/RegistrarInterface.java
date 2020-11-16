package registrar;

import bar.Bar;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RemoteRef;

public interface RegistrarInterface extends Remote{
    void sendMessage(String message, Bar bar);
}
