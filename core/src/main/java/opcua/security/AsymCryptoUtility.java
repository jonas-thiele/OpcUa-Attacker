package opcua.security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides utility functions for asymmetric cryptography
 */
public class AsymCryptoUtility {

    /**
     * Returns the plaintext block size
     * @param algorithm Asymmetric security algorithm used for encryption
     * @param key The public key for encryption
     * @return Plaintext block size
     */
    public static int getAsymPlainTextBlockSize(SecurityAlgorithm algorithm, RSAPublicKey key) {
        switch (algorithm) {
            case RSA_15:
                return key.getModulus().bitLength() / 8 - 11; //Padding >= 11 bytes
            case RSA_OAEP:
                return key.getModulus().bitLength() / 8 - 42;
            default:
                throw new Error("Invalid algorithm");
        }
    }

    /**
     * Generates the padding field of an OPC UA message
     * @param paddingLength Length of the padding
     * @param useExtraPaddingSize For extra long paddings
     * @return Padding bytes
     */
    public static byte[] generatePadding(int paddingLength, boolean useExtraPaddingSize) {
        byte paddingByte = (byte)(paddingLength & 0xFF);
        int combinedLength = useExtraPaddingSize ? paddingLength + 2 : paddingLength + 1;
        byte[] result = new byte[combinedLength];

        //The first iteration writes the PaddingSize field
        for(int i=0; i<paddingLength+1; i++) {
            result[i] = paddingByte;
        }
        if(useExtraPaddingSize) {
            result[combinedLength-1] =(byte)(paddingByte >> 8); //ExtraPaddingSize field (most significant byte)
        }

        return result;
    }

    /**
     * Compute signature for bytes
     * @param privateKey The private key for signing
     * @param bytesToSign The bytes to sign
     * @param securityPolicy Security policy in use
     * @return The signature
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public static byte[] sign(RSAPrivateKey privateKey, byte[] bytesToSign, SecurityPolicy securityPolicy)
            throws InvalidKeyException, SignatureException {
        try {
            switch (securityPolicy.getAsymmetricSignature()) {
                case RSA_SHA1: {
                    Signature signature = Signature.getInstance("SHA1withRSA");
                    signature.initSign(privateKey);
                    signature.update(bytesToSign);
                    return signature.sign();
                }
                case RSA_SHA256: {
                    Signature signature = Signature.getInstance("SHA256withRSA");
                    signature.initSign(privateKey);
                    signature.update(bytesToSign);
                    return signature.sign();
                }
                case RSA_PSS_SHA256: {
                    Signature signature = Signature.getInstance("SHA256withRSAandMGF1");
                    signature.initSign(privateKey);
                    signature.update(bytesToSign);
                    return signature.sign();
                }
                default: throw new Error("Invalid SecurityPolicy");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new Error(e);
        }
    }

    /**
     * Encrypts a byte array
     * @param publicKey Public key for encryption
     * @param bytesToEncrypt The byte to encrypt
     * @param securityPolicy Security policy in use
     * @return Ciphertext
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public static byte[] encrypt(RSAPublicKey publicKey, byte[] bytesToEncrypt, SecurityPolicy securityPolicy)
            throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        try {
            switch (securityPolicy.getAsymmetricEncryption()) {
                case RSA_15: {
                    Cipher cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding");
                    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
                    return cipher.doFinal(bytesToEncrypt);
                }
                case RSA_OAEP: {
                    Cipher cipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA1AndMGF1Padding");
                    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
                    return cipher.doFinal(bytesToEncrypt);
                }
                default: throw new Error("Invalid SecurityPolicy");
            }
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            throw new Error(e);
        }
    }

    /**
     * Convert BigInteger to corresponding byte array
     * @param bigInt The integer
     * @param outputSize Size of the output array
     * @return Output array
     */
    public static byte[] convertBigInteger(BigInteger bigInt, int outputSize) {
        byte[] temp = bigInt.toByteArray();

        if(temp[0] == 0 && temp.length > outputSize) {  //Remove leading 0
            byte[] result = new byte[temp.length - 1];
            System.arraycopy(temp,1, result, 0, result.length);
            return result;
        }
        if(temp.length < outputSize) {   //Pad with leading 0's
            byte[] result = new byte[outputSize];
            System.arraycopy(temp, 0, result, outputSize - temp.length, temp.length);
            return result;
        }
        return temp;
    }
}
