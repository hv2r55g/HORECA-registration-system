package matchingService;

import customer.Bezoek;
import doctor.CriticalTuple;
import mixingProxy.Capsule;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface MatchingServiceInterface extends Remote {

    void addCapsules(List<Capsule> capsules) throws RemoteException;

    void receiveInfectedBezoeken(List<Bezoek> infectedBezoeken) throws RemoteException;

    List requestCriticalTuples() throws RemoteException;
}
