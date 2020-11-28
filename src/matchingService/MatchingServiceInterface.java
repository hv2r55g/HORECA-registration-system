package matchingService;

import mixingProxy.Capsule;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MatchingServiceInterface extends Remote {

    void addCapsule(Capsule capsule) throws RemoteException;
}
