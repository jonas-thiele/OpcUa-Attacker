package attacks.manger.oracle;

import attacks.CipherTextHelper;
import opcua.context.Context;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;

public class TimingOracle extends Oracle {

    private Context context;
    private final long threshold;
    private final boolean lessThanBTakesLonger;
    private byte[] validCipherText;
    private int blockNumber;
    private int blockOffset;
    private int cipherBlockSize;

    public TimingOracle(Context context, long threshold, boolean lessThanBTakesLonger, byte[] validCipherText, int blockNumber) throws IOException {
        this.context = context;
        this.threshold = threshold;
        this.lessThanBTakesLonger = lessThanBTakesLonger;
        this.validCipherText = validCipherText;
        RSAPublicKey publicKey = (RSAPublicKey) context.getRemoteCertificate().getPublicKey();
        this.blockNumber = blockNumber;
        this.blockOffset = CipherTextHelper.getOpnBlockOffset(validCipherText, publicKey);
        this.cipherBlockSize = publicKey.getModulus().bitLength() / 8;
    }

    @Override
    public boolean checkValidity(byte[] message) throws OracleException {
        return false;
    }
}
