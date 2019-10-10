package attacks.manger.oracle.timing;

import java.util.Arrays;

/**
 * Contains a variety of utility functions for filtering.
 */
public class Filter {

    /**
     * I-th percentile
     * @param samples An array of sample sets
     * @param numerator The numerator-th lowest element in a sample set is chosen
     * @return An array of filtered values
     */
    public static double[] ithPercentile(long[][] samples, int numerator) {
        long[][] temp = deepCopy(samples);
        return ithPercentileImpl(temp, numerator);
    }

    /**
     * I-th percentile
     * @param samples A sample set
     * @param numerator The numerator-th lowest element in a sample set is chosen
     * @return The filtered value
     */
    public static double ithPercentile(long[] samples, int numerator) {
        long[] temp = Arrays.copyOf(samples, samples.length);
        Arrays.sort(temp);
        return temp[numerator];
    }

    /**
     * Finds the percentile with minimal combined empirical standard deviation for different two sample sets
     * @param samples1 Sample set for class C1
     * @param samples2 Sample set for class C2
     * @param min The minimal percentile numerator to consider
     * @param max The maximal percentile numerator to consider
     * @return The numerator of the best performing percentile
     */
    public static int findBestPercentile(long[][] samples1, long[][] samples2, int min, int max) {
        long[][] temp1 = deepCopy(samples1);
        long[][] temp2 = deepCopy(samples2);
        double minS = Double.MAX_VALUE;
        int minOrder = -1;
        for(int i=min; i<max; i++) {
            double empiricalStandardDeviation1 = empiricalStandardDeviation(ithPercentileImpl(temp1, i));
            double empiricalStandardDeviation2 = empiricalStandardDeviation(ithPercentileImpl(temp2, i));
            double s = (empiricalStandardDeviation1 + empiricalStandardDeviation2) / 2;
            if(s < minS) {
                minS = s;
                minOrder = i;
            }
        }
        return minOrder;
    }

    /**
     * Returns the empirical mean of a sample set
     */
    public static double empiricalMean(double[] data) {
        double sum = 0;
        for(double point : data) {
            sum += point;
        }
        return sum / data.length;
    }

    /**
     * Returns the empirical standard deviation of a sample set
     */
    public static double empiricalStandardDeviation(double[] data) {
        double temp = 0;
        double empiricalMean = empiricalMean(data);
        for(double point : data) {
            temp += Math.pow((point - empiricalMean), 2);
        }
        return Math.sqrt(temp / (data.length-1));
    }

    /**
     * Returns the mean empirical standard deviation of two different sample sets
     */
    public static double meanEmpiricalStandardDeviation(double[] data1, double[] data2) {
        double s1 = empiricalStandardDeviation(data1);
        double s2 = empiricalStandardDeviation(data2);
        return (s1+s2)/2;
    }



    private static long[][] deepCopy(long[][] original) {
        long[][] result = new long[original.length][];
        for (int i = 0; i < original.length; i++) {
            result[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return result;
    }

    private static double[] ithPercentileImpl(long[][] samples, int order) {
        double[] result = new double[samples.length];
        for(int i=0; i<samples.length; i++) {
            Arrays.sort(samples[i]);
            result[i] = samples[i][order];
        }
        return result;
    }

    private static long[][] deepCopy(long[][] original, int len1, int len2) {
        long[][] result = new long[len1][];
        for (int i = 0; i < original.length; i++) {
            result[i] = Arrays.copyOf(original[i], len2);
        }
        return result;
    }
}
