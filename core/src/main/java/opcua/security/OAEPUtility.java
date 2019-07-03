package opcua.security;

import opcua.util.DataTypeConverter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class OAEPUtility {

    public static MessageDigest MGF_HASH;
    public static int H_LENGTH;

    static {
        try {
            MGF_HASH = MessageDigest.getInstance("SHA-1");
            H_LENGTH = MGF_HASH.getDigestLength();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * Decodes an already decrypted RSA-OAEP block as described in RFC 2437, section 9.1.1.2
     * This function does not perform any checks against the encoding parameters.
     */

    public static byte[] removeLeadingNullByte(byte[] block) {
        if(block[0] != 0) {
            return block;
        }
        byte[] result = new byte[block.length-1];
        System.arraycopy(block, 1, result, 0, result.length);
        return result;
    }

    public static byte[] decode(byte[] encodedBlock) {
        encodedBlock = removeLeadingNullByte(encodedBlock);
        byte[] maskedSeed = Arrays.copyOfRange(encodedBlock, 0, H_LENGTH);
        byte[] maskedDataBlock = Arrays.copyOfRange(encodedBlock, H_LENGTH, encodedBlock.length);

        byte[] seedMask = maskGeneratorFunction(maskedDataBlock, H_LENGTH);
        byte[] seed = xor(maskedSeed, seedMask);

        byte[] dataBlockMask = maskGeneratorFunction(seed, encodedBlock.length - H_LENGTH);
        byte[] dataBlock = xor(maskedDataBlock, dataBlockMask);

        //We don't need the parameters-hash and padding bytes
        int secretOffset = H_LENGTH;
        while(secretOffset < dataBlock.length-1 && dataBlock[secretOffset] == 0) {
            secretOffset++;     //Skip through PS
        }
        if(dataBlock[secretOffset] != 1) {
            throw new Error("TODO");
        }
        secretOffset++;

        return Arrays.copyOfRange(dataBlock, secretOffset, dataBlock.length);
    }

    /**
     * MGF1 as described in RFC 2437, section 10.2.1
     */
    public static byte[] maskGeneratorFunction(byte[] seed, int length) {
        MGF_HASH.reset();
        byte[] mask = new byte[length];

        int i;
        for(i=0; i<length/H_LENGTH; i++) {
            byte[] counterBytes = DataTypeConverter.uInt32ToBytesBE(i);
            MGF_HASH.update(seed);
            MGF_HASH.update(counterBytes);
            byte[] hashResult = MGF_HASH.digest();
            System.arraycopy(hashResult, 0, mask, i*H_LENGTH, H_LENGTH);
        }
        if(i * H_LENGTH < length) {
            byte[] counterBytes = DataTypeConverter.uInt32ToBytesBE(i);
            MGF_HASH.update(seed);
            MGF_HASH.update(counterBytes);
            byte[] hashResult = MGF_HASH.digest();
            System.arraycopy(hashResult, 0, mask, i*H_LENGTH, mask.length - (i*H_LENGTH));
        }

        return mask;
    }


    private static byte[] xor(byte[] lhs, byte[] rhs) {
        assert(lhs.length == rhs.length);
        byte[] result = new byte[lhs.length];
        for(int i=0; i<lhs.length; i++) {
            result[i] = (byte)(lhs[i]^rhs[i]);
        }
        return result;
    }
}
