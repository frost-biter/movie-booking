package com.movie.bookMyShow.util;

import org.bitcoinj.params.MainNetParams;
import org.web3j.crypto.Keys;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.DeterministicKey;
import java.math.BigInteger;


public class AddressUtil {

//    public static String deriveEthAddressFromXpub(String xpub, int index) {
//        System.out.print("\nCreating address for index: " + index +"\n");
//        DeterministicKey parent = DeterministicKey.deserializeB58(xpub, null);
//        DeterministicKey child = HDKeyDerivation.deriveChildKey(parent, new ChildNumber(index, false));
//        return "0x" + Keys.getAddress(String.valueOf(child.getPubKeyPoint()));
//    }
//    public static String deriveEthAddressFromXpub(String xpub, int index) {
//        System.out.println("\nCreating address for index: " + index + "\n");
//
//        DeterministicKey parentKey = DeterministicKey.deserializeB58(xpub, MainNetParams.get());
//        DeterministicKey childKey = HDKeyDerivation.deriveChildKey(parentKey, index);
//        byte[] publicKeyBytes = childKey.getPubKey();
//        ECKey ecKey = ECKey.fromPublicOnly(publicKeyBytes);
//
//        return "0x" + Keys.getAddress(ecKey.getPublicKeyAsHex());
////        return childKey.serializePubB58(MainNetParams.get());
//
//    }
public static String deriveEthAddressFromXpub(String xpub, int index) {
    System.out.println("\nCreating address for index: " + index + "\n");

    // 1. Deserialize and derive key
    DeterministicKey parentKey = DeterministicKey.deserializeB58(xpub, MainNetParams.get());
    DeterministicKey childKey = HDKeyDerivation.deriveChildKey(parentKey, index);

    // 2. Convert to Ethereum address
    byte[] pubKeyBytes = childKey.getPubKey();
    BigInteger publicKey = new BigInteger(1, pubKeyBytes); // Using raw bytes

    return "0x" + Keys.getAddress(publicKey);
}
}