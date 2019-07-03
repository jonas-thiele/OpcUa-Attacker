package attacks.manger.oracle;


import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Abstraction for a PKCS#1 padding oracle.
 */

public abstract class Oracle {

    private long queryCount;

    public abstract boolean checkValidity(byte[] message) throws OracleException;


    protected void incrementQueryCount() {
        queryCount++;
    }

    public long getQueryCount() {
        return queryCount;
    }

}
