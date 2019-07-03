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

public class CryptoUtility {

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

    public static byte[] sign(RSAPrivateKey privateKey, byte[] bytesToSign, SecurityPolicy securityPolicy)
            throws InvalidKeyException, SignatureException {
        if(securityPolicy.getAsymmetricSignature() == SecurityAlgorithm.RSA_SHA1) {
            try {
                Signature signature = Signature.getInstance("SHA1withRSA");
                signature.initSign(privateKey);
                signature.update(bytesToSign);
                return signature.sign();
            }
            catch (NoSuchAlgorithmException e) {
                throw new Error(e);
            }
        }
        else if(securityPolicy.getAsymmetricSignature() == SecurityAlgorithm.RSA_SHA256) {
            try {
                Signature signature = Signature.getInstance("SHA256withRSA");
                signature.initSign(privateKey);
                signature.update(bytesToSign);
                return signature.sign();
            }
            catch (NoSuchAlgorithmException e) {
                throw new Error(e);
            }
        }
        else {
            throw new Error("Invalid SecurityPolicy");
        }
    }

    public static byte[] encrypt(RSAPublicKey publicKey, byte[] bytesToEncrypt, SecurityPolicy securityPolicy)
            throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        if(securityPolicy.getAsymmetricEncryption() == SecurityAlgorithm.RSA_15) {
            try {
                Cipher cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding");
                cipher.init(Cipher.ENCRYPT_MODE, publicKey);
                return cipher.doFinal(bytesToEncrypt);
            }
            catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
                throw new Error(e);
            }
        }
        else if(securityPolicy.getAsymmetricEncryption() == SecurityAlgorithm.RSA_OAEP) {
            try {
                Cipher cipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA1AndMGF1Padding");
                cipher.init(Cipher.ENCRYPT_MODE, publicKey);
                return cipher.doFinal(bytesToEncrypt);
            }
            catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
                throw new Error(e);
            }
        }
        else {
            throw new Error("Invalid SecurityPolicy");
        }
    }

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
