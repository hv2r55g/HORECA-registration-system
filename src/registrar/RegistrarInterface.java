package registrar;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import mixingProxy.Capsule;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.*;
import java.util.List;

public interface RegistrarInterface extends Remote {

    List requestMonthlyNyms(String bussinesNumber) throws IOException, NoSuchAlgorithmException, InvalidKeyException;

    List requestDailyCustomerToken(String phoneNumber) throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException;

    PublicKey getPublicKeyOfTheDay() throws RemoteException;

    ListMultimap<String, String> getMappingDayNyms(int incubatieTijd) throws RemoteException;

    void sendUninformedCustomers(List<Capsule> neededCapsules)throws RemoteException;

}

