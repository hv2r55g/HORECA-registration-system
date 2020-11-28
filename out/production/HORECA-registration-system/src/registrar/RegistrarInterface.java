package registrar;

import java.io.UnsupportedEncodingException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.*;
import java.util.List;

public interface RegistrarInterface extends Remote {

    List requestMonthlyHash(int bussinesNumber) throws RemoteException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException;

    List requestDailyCustomerToken(String phoneNumber) throws RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException;

    PublicKey getPublicKeyOfTheDay() throws RemoteException;

    PrivateKey getPrivatekeyOftheDay() throws RemoteException;

    String getDagVanVandaag() throws RemoteException;

}

