package opcua.security;

import java.security.PrivateKey;
import java.security.PublicKey;

public interface CryptoProvider {

    byte[] encryptAysymm(PublicKey key, SecurityPolicy policy, byte[] plaintext);

    byte[] decryptAsymm(PrivateKey key, SecurityPolicy policy, byte[] ciphertext);

    byte[] encryptSymm(byte[] key, byte[] iv, SecurityPolicy policy, byte[] plaintext);

    byte[] decryptSymm(byte[] key, byte[] iv, SecurityPolicy policy, byte[] plaintext);

    byte[] signAsymm(PrivateKey key, SecurityPolicy policy, byte[] data);

    boolean verifyAsymm(PublicKey key, SecurityPolicy policy, byte[] dataToVerify, byte[] signature);

    byte[] signSymm(byte[] key, SecurityPolicy policy, byte[] data);

    boolean verifySymm(byte[] key, SecurityPolicy policy, byte[] dataToVerify, byte[] signature);
}
