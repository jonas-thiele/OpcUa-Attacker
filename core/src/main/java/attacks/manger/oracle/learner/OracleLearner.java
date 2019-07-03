package attacks.manger.oracle.learner;

import attacks.manger.oracle.Oracle;
import attacks.manger.oracle.OracleException;

public abstract class OracleLearner {

    public abstract LearningResult learn(int rounds) throws OracleException;


    public class LearningResult {
        private final Oracle oracle;
        private final double confidence;

        public LearningResult(Oracle oracle, double confidence) {
            this.oracle = oracle;
            this.confidence = confidence;
        }

        public Oracle getOracle() {
            return oracle;
        }

        public double getConfidence() {
            return confidence;
        }
    }
}
