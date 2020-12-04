package matchingService;

import customer.Bezoek;
import mixingProxy.Capsule;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface MatchingServiceInterface extends Remote {

    void addCapsules(List<Capsule> capsules) throws RemoteException;

    boolean requestInfectedOrNot(List<Capsule> capsules) throws RemoteException;

    void setInfectedCapsules(List<Bezoek> bezoekenPatient) throws RemoteException;
}
