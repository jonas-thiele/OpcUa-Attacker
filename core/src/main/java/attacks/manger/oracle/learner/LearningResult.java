package attacks.manger.oracle.learner;

import attacks.manger.oracle.Oracle;
import reporting.entry.Entry;

/**
 * This class is returned by any OracleLearner
 */
public class LearningResult {
    private final Oracle oracle;
    private final Entry report;
    private final boolean successful;

    /**
     * Constructor
     * @param oracle The learned oracle
     * @param report A report that documents the testing process
     * @param successful Indicate, whether an oracle was inferred successfully
     */
    public LearningResult(Oracle oracle, Entry report, boolean successful) {
        this.oracle = oracle;
        this.report = report;
        this.successful = successful;
    }

    /**
     * Constructor for successful learning result
     * @param oracle The learned oracle
     * @param report A report that documents the testing process
     */
    public LearningResult(Oracle oracle, Entry report) {
        this.oracle = oracle;
        this.report = report;
        this.successful = true;
    }

    /**
     * Constructor for an unsuccessful learning result.
     * @param report A report that documents the testing process
     */
    public LearningResult(Entry report) {
        this.report = report;
        this.oracle = null;
        this.successful = false;
    }

    public Oracle getOracle() {
        return oracle;
    }

    public Entry getReport() {
        return report;
    }

    public boolean isSuccessful() {
        return successful;
    }
}
