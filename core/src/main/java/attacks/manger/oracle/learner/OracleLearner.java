package attacks.manger.oracle.learner;

import attacks.manger.oracle.OracleException;
import opcua.context.Endpoint;

/**
 * Tries to find a specific vulnerability and create an oracle that exploit it.
 */
public interface OracleLearner {
    LearningResult learn(Endpoint endpoint) throws OracleException;
}
