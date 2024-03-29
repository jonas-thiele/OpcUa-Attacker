package attacks.manger.oracle;


import java.math.BigInteger;
import java.security.interfaces.RSAPrivateKey;

/**
 * This is an oracle for testing purposes only. It knows the private key and will always return the correct result.
 */
public class TestOracle extends Oracle {
    private final BigInteger d;
    private final BigInteger N;
    private final BigInteger B;

    /**
     * Constructor
     * @param privateKey Private key for decryption
     */
    public TestOracle(RSAPrivateKey privateKey) {
        this.d = privateKey.getPrivateExponent();
        this.N = privateKey.getModulus();

        this.B = BigInteger.ONE.shiftLeft(N.bitLength() - 8);
    }

    @Override
    public boolean checkValidity(byte[] message) throws OracleException {
        incrementQueryCount();
        BigInteger c = new BigInteger(1, message);
        BigInteger m = c.modPow(d, N);

        return m.compareTo(B) < 0;
    }

    public BigInteger getB() {
        return B;
    }
}
