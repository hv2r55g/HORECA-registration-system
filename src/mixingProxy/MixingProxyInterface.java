package mixingProxy;

import registrar.Token;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

public interface MixingProxyInterface extends Remote {
    boolean sendCapsule(Capsule capsule) throws RemoteException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, SignatureException;

    String signCapsule(Capsule capsule) throws NoSuchAlgorithmException, SignatureException, RemoteException, InvalidKeyException;

    Capsule requestLeaving(Token currentToken) throws RemoteException;

    String requestLogoOfTheDay(String hashBar) throws RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException;
}
