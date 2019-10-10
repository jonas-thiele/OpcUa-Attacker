import attacks.CipherTextUtility;
import attacks.manger.oracle.OracleException;
import attacks.manger.oracle.VictimProxy;
import opcua.context.Endpoint;
import opcua.context.LocalKeyPair;
import opcua.encoding.EncodingException;
import opcua.message.OpenSecureChannelRequest;
import opcua.message.parts.RequestHeader;
import opcua.model.type.SecurityTokenRequestType;
import opcua.model.type.EndpointDescription;
import opcua.model.type.ObjectIds;
import opcua.security.MessageSecurityMode;
import opcua.security.SecurityPolicy;
import opcua.util.CommonMessageFlows;
import opcua.util.MessageUtility;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import transport.SecureChannelUtil;
import transport.TransportException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.function.Function;

public class TimingTest {

    private final static int SAMPLE_SIZE = 100000;
    private final Endpoint endpoint;
    private final int cipherBlockLength;
    private final VictimProxy victimProxy;

    public TimingTest(Endpoint endpoint, byte[] validCipherText, int blockNumber) throws OracleException {
        this.endpoint = endpoint;
        int blockOffset;
        try {
            blockOffset = CipherTextUtility.getOpnBlockOffset(validCipherText, endpoint.getPublicKey());
        } catch (IOException e) {
            throw new OracleException("Unable to compute block offset", e);
        }
        this.cipherBlockLength = endpoint.getPublicKey().getModulus().bitLength() / 8;
        victimProxy = new VictimProxy(endpoint, validCipherText, blockOffset, blockNumber);
    }



    public static void main(String[] args) throws CertificateException, EncodingException, OracleException, IOException, TransportException {
        Security.addProvider(new BouncyCastleProvider());

        LocalKeyPair localKeyPair = LocalKeyPair.generateSelfSigned(2048, "CN=OpcUaAttacker");

        Endpoint uaJava = new Endpoint(
                "192.168.178.41",
                8666,
                "opc.tcp://192.168.178.41:8666/UAExample"
        );

        EndpointDescription[] endpointDescriptions = CommonMessageFlows.retrieveEndpointDescriptions(uaJava);
        for(EndpointDescription endpointDescription : endpointDescriptions) {
            if(endpointDescription.getSecurityPolicy() == SecurityPolicy.BASIC256_SHA256 && endpointDescription.getSecurityMode() == MessageSecurityMode.SIGN_AND_ENCRYPT) {
                uaJava.setSecurityPolicy(SecurityPolicy.BASIC256_SHA256);
                uaJava.setMessageSecurityMode(MessageSecurityMode.SIGN_AND_ENCRYPT);
                uaJava.setCertificate(endpointDescription.getServerCertificate());
            }
        }

        //Generate the client nonce
        byte[] clientNonce = SecureChannelUtil.generateRandomNonce(uaJava.getSecurityPolicy());

        //Opn Message
        RequestHeader requestHeader = new RequestHeader(ObjectIds.OPEN_SECURE_CHANNEL_REQUEST, null, 0, 0, "", 5000);
        OpenSecureChannelRequest openSecureChannelRequest = new OpenSecureChannelRequest(
                requestHeader, 0, SecurityTokenRequestType.ISSUE, MessageSecurityMode.SIGN_AND_ENCRYPT, clientNonce, 5000
        );
        byte[] cipherText = MessageUtility.getSignedEncrypted(openSecureChannelRequest, uaJava, localKeyPair);

        TimingTest timingTest = new TimingTest(uaJava, cipherText, 0);
        timingTest.runTests();
    }


    public void runTests() throws OracleException, IOException {

        Function<long[][], double[]> mean = samples -> {
            double[] result = new double[samples.length];
            for(int i=0; i<samples.length; i++) {
                long sum = 0;
                for(long sample : samples[i]) {
                    sum += sample;
                }
                result[i] = ((double)sum)/ samples[i].length;
            }
            return result;
        };

        Function<long[][], double[]> median = samples -> {
            long[][] temp = deepCopy(samples);
            double[] result = new double[temp.length];
            for(int i=0; i<temp.length; i++) {
                Arrays.sort(temp[i]);
                if(temp[i].length % 2 == 1) {
                    result[i] = temp[i][temp[i].length/2];
                } else {
                    result[i] = (temp[i][temp[i].length/2 -1] + temp[i][temp[i].length/2]) / 2;
                }
            }
            return result;
        };

        Function<long[][], double[]> lowerMeanAround1stPercentile = samples -> {
            long[][] temp = deepCopy(samples);
            double[] result = new double[temp.length];
            int sampleSize = temp[0].length;
            int lowerIndex = (int)(0.005*sampleSize);
            int upperIndex = (int)(0.02*sampleSize);
            for(int i=0; i<temp.length; i++) {
                Arrays.sort(temp[i]);
                long sum = 0;
                for(int j=lowerIndex; j<upperIndex; j++) {
                    sum += temp[i][j];
                }
                result[i] = ((double)sum)/(upperIndex-lowerIndex);
            }
            return result;
        };

        Function<long[][], double[]> firstPercentile = samples -> {
            long[][] temp = deepCopy(samples);
            double[] result = new double[temp.length];
            for(int i=0; i<temp.length; i++) {
                Arrays.sort(temp[i]);
                result[i] = temp[i][temp[i].length / 100];
            }
            return result;
        };

        Function<long[][], double[]> z0point3Percentile = samples -> {
            long[][] temp = deepCopy(samples);
            double[] result = new double[temp.length];
            for(int i=0; i<temp.length; i++) {
                Arrays.sort(temp[i]);
                result[i] = temp[i][(int)(0.003*temp[i].length)];
            }
            return result;
        };

        Function<long[][], double[]> min = samples -> {
            long[][] temp = deepCopy(samples);
            double[] result = new double[temp.length];
            for(int i=0; i<temp.length; i++) {
                Arrays.sort(temp[i]);
                result[i] = temp[i][0];
            }
            return result;
        };
        /*

        long[][] samples10000 = produceSamplesSequentially(120, 10000);


        savePlotData(mean.apply(samples1000), "mean-sequential-1000");
        savePlotData(median.apply(samples1000), "median-sequential-1000");
        savePlotData(min.apply(samples1000), "min-sequential-1000");
        savePlotData(lowerMeanAround1stPercentile.apply(samples1000), "lowerMeanAround1stPercentile-sequential-1000");
        savePlotData(firstPercentile.apply(samples1000), "firstPercentile-sequential-1000");

        long[][] samples10000 = produceSamplesSequentially(50, 10000);

        savePlotData(mean.apply(samples10000), "mean-sequential-10000");
        savePlotData(median.apply(samples10000), "median-sequential-10000");
        savePlotData(min.apply(samples10000), "min-sequential-10000");
        savePlotData(lowerMeanAround1stPercentile.apply(samples10000), "lowerMeanAround1stPercentile-sequential-10000");
        savePlotData(firstPercentile.apply(samples10000), "firstPercentile-sequential-10000");

        long[][] samplesInterleaved1000 = produceSamplesInterleaved(50, 1000);
        long[][] samplesInterleaved10000 = produceSamplesSequentially(50, 10000);

        savePlotData(mean.apply(samplesInterleaved1000), "mean-interleaved-1000");
        savePlotData(median.apply(samplesInterleaved1000), "median-interleaved-1000");
        savePlotData(min.apply(samplesInterleaved1000), "min-interleaved-1000");
        savePlotData(lowerMeanAround1stPercentile.apply(samplesInterleaved1000), "lowerMeanAround1stPercentile-interleaved-1000");
        savePlotData(firstPercentile.apply(samplesInterleaved1000), "firstPercentile-interleaved-1000");
        savePlotData(mean.apply(samplesInterleaved10000), "mean-interleaved-10000");
        savePlotData(median.apply(samplesInterleaved10000), "median-interleaved-10000");
        savePlotData(min.apply(samplesInterleaved10000), "min-interleaved-10000");
        savePlotData(lowerMeanAround1stPercentile.apply(samplesInterleaved10000), "lowerMeanAround1stPercentile-interleaved-10000");
        savePlotData(firstPercentile.apply(samplesInterleaved10000), "firstPercentile-interleaved-10000");
*/

        /*
        long[][] samplesSequentially = produceSamplesSequentially(50, 1000);
        long[][] samplesInterleaved = produceSamplesInterleaved(50, 1000);

        savePlotData(firstPercentile.apply(samplesSequentially), "1percentile-sequentially");
        savePlotData(firstPercentile.apply(samplesInterleaved), "1percentile-interleaved");

         */


        long[][] loadedSamples = loadSamples(120);
        int k = loadedSamples.length;
        System.out.println(k);


        saveSampleSizeStatistics(firstPercentile, loadedSamples, "firstPercentile1");
        saveSampleSizeStatistics(min, loadedSamples, "min");
        saveSampleSizeStatistics(z0point3Percentile, loadedSamples, "0_3stPercentile1");
        saveSampleSizeStatistics(median, loadedSamples, "median1");
        saveSampleSizeStatistics(mean, loadedSamples, "mean1");
        saveSampleSizeStatistics(lowerMeanAround1stPercentile, loadedSamples, "lowerMeanAround1stPercentile1");


        long[][] samples2 = new long[k][100];
        for(int i=0; i<k; i++) {
            samples2[i] = Arrays.copyOf(loadedSamples[i], 100);
        }

        savePlotData(mean.apply(samples2), "mean-sequential1");
        savePlotData(median.apply(samples2), "median-sequential1");
        savePlotData(min.apply(samples2), "min-sequential1");
        savePlotData(lowerMeanAround1stPercentile.apply(samples2), "lowerMeanAround1stPercentile-sequential1");
        savePlotData(firstPercentile.apply(samples2), "firstPercentile-sequential1");


/*
        byte[] plainBlock = MangerUtility.generatePlaintextLessB(endpoint.getPublicKey());

        for(int i=0; i<200; i++) {
            try {
                long pre = System.nanoTime();
                long[] samples = produceSamplesSequentially(100000, plainBlock);
                saveSamples(samples);
                System.out.println("---------" + (i+1) + "--------- (" + (System.nanoTime() - pre)/60000000000L + ")");
            } catch (OracleException e) {
                System.out.println(e.toString());
                try {
                    synchronized (this) {
                        this.wait(60000);
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
*/



        long[][] samples10000 = new long[loadedSamples.length][10000];
        long[][] samples1000 = new long[loadedSamples.length][1000];

        for(int j=0; j<loadedSamples.length; j++) {
            samples10000[j] = Arrays.copyOf(loadedSamples[j], 10000);
            samples1000[j] = Arrays.copyOf(loadedSamples[j], 1000);
            Arrays.sort(loadedSamples[j]);
            Arrays.sort(samples10000[j]);
            Arrays.sort(samples1000[j]);
        }
        double[] percentileValue = new double[100000];
        double[] percentileValue10000 = new double[10000];
        double[] percentileValue1000 = new double[1000];
        for(int i=0; i<100000; i++) {
            double[] result = new double[loadedSamples.length];
            for(int j=0; j<loadedSamples.length; j++) {
                result[j] = loadedSamples[j][i];
            }
            percentileValue[i] = getEmpiricalStandardDeviation(result);
            if(i % 10000 == 0) System.out.println(i);
        }

        for(int i=0; i<10000; i++) {
            double[] result = new double[samples10000.length];
            for(int j=0; j<samples10000.length; j++) {
                result[j] = samples10000[j][i];
            }
            percentileValue10000[i] = getEmpiricalStandardDeviation(result);
            if(i % 1000 == 0) System.out.println(i);
        }

        for(int i=0; i<1000; i++) {
            double[] result = new double[samples1000.length];
            for(int j=0; j<samples1000.length; j++) {
                result[j] = samples1000[j][i];
            }
            percentileValue1000[i] = getEmpiricalStandardDeviation(result);
            if(i % 100 == 0) System.out.println(i);
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Users\\Jonas\\Desktop\\Bachelorarbeit\\thesis\\figures\\timings\\percentileStats1.dat"));
        writer.append("i deviation\n");
        for(int i=0; i<20000; i++) {
            writer.append(Integer.toString(i)).append(" ").append(Double.toString(percentileValue[i])).append("\n");
        }
        writer.close();

        writer = new BufferedWriter(new FileWriter("C:\\Users\\Jonas\\Desktop\\Bachelorarbeit\\thesis\\figures\\timings\\percentileStats10k1.dat"));
        writer.append("i deviation\n");
        for(int i=0; i<2000; i++) {
            writer.append(Integer.toString(i*10)).append(" ").append(Double.toString(percentileValue10000[i])).append("\n");
        }
        writer.close();

        writer = new BufferedWriter(new FileWriter("C:\\Users\\Jonas\\Desktop\\Bachelorarbeit\\thesis\\figures\\timings\\percentileStats1k1.dat"));
        writer.append("i deviation\n");
        for(int i=0; i<200; i++) {
            writer.append(Integer.toString(i*100)).append(" ").append(Double.toString(percentileValue1000[i])).append("\n");
        }
        writer.close();



    }

    private void saveSampleSizeStatistics(Function<long[][], double[]> filter, long[][] samples, String name) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Users\\Jonas\\Desktop\\Bachelorarbeit\\thesis\\figures\\timings\\sampleSizeStat-"+ name +".dat"));
        writer.append("sampleSize standardDeviation\n");

        int k = samples.length;

        for(int exponent = 0; exponent < 3; exponent++) {
            for(int intermediate = 1; intermediate < 10; intermediate++) {
                // We only need a subset of samples
                int subsetSize = intermediate * 100 * (int)Math.pow(10, exponent);
                long[][] subset = new long[k][subsetSize];
                for(int i=0; i<k; i++) {
                    subset[i] = Arrays.copyOf(samples[i], subsetSize);
                }

                double[] filteredData = filter.apply(subset);
                double standardDeviation = getEmpiricalStandardDeviation(filteredData);

                writer.append(Integer.toString(subsetSize)).append(" ").append(Double.toString(standardDeviation)).append("\n");
            }
        }

        double[] filteredData = filter.apply(samples);
        double standardDeviation = getEmpiricalStandardDeviation(filteredData);

        writer.append("100000").append(" ").append(Double.toString(standardDeviation));

        writer.close();
    }

    public long[] produceSamplesSequentially(int sampleSize, byte[] plainBlock) throws OracleException {
        for (int i = 0; i < sampleSize / 100; i++) {
            measureTiming(plainBlock);
        }

        long[] timings = new long[sampleSize];
        for (int i = 0; i < sampleSize; i++) {
            if(i % 10000 == 0) {
                System.out.println(i);
            }
            timings[i] = measureTiming(plainBlock);
        }
        return timings;
    }

    public long[][] produceSamplesSequentially(int rounds, int sampleSize) throws OracleException {
        SecureRandom secureRandom = new SecureRandom();
        byte[][] plainBlocks = new byte[rounds][cipherBlockLength];
        for(int i = 0; i < rounds; i++) {
            secureRandom.nextBytes(plainBlocks[i]);
            plainBlocks[i][0] = (byte)0;
        }

        //warm up
        for(int i = 0; i < rounds; i++) {
            measureTiming(plainBlocks[i]);
        }

        long[][] timings = new long[rounds][sampleSize];
        for(int i = 0; i < rounds; i++) {
            for(int j = 0; j < sampleSize; j++) {
                timings[i][j] = measureTiming(plainBlocks[i]);
                if(j % 10000 == 0) {
                    System.out.println(i + ":" + j);
                }
            }
        }

        return timings;
    }

    public long[][] produceSamplesInterleaved(int rounds, int sampleSize) throws OracleException {
        SecureRandom secureRandom = new SecureRandom();
        byte[][] plainBlocks = new byte[rounds][cipherBlockLength];
        for(int i = 0; i < rounds; i++) {
            secureRandom.nextBytes(plainBlocks[i]);
            plainBlocks[i][0] = (byte)0;
        }

        //warm up
        for(int i = 0; i < rounds; i++) {
            measureTiming(plainBlocks[i]);
        }

        long[][] timings = new long[rounds][sampleSize];
        for(int j = 0; j < sampleSize; j++) {
            for(int i = 0; i < rounds; i++) {
                timings[i][j] = measureTiming(plainBlocks[i]);
            }
        }

        return timings;
    }

    private void saveSamples(long[] data) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Users\\Jonas\\Desktop\\Bachelorarbeit\\thesis\\figures\\timings\\samples2.data", true));
        for(long timing : data) {
            writer.append(Long.toString(timing)).append(" ");
        }
        writer.append("\n");
        writer.close();
    }

    private void saveSamples(long[][] data) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Users\\Jonas\\Desktop\\Bachelorarbeit\\thesis\\figures\\timings\\samples2.data", true));
        for(long[] set : data) {
            for(long timing : set) {
                writer.append(Long.toString(timing)).append(" ");
            }
            writer.append("\n");
        }
        writer.close();
    }

    private long[][] loadSamples(int max) throws IOException {
        String path = "C:\\Users\\Jonas\\Desktop\\Bachelorarbeit\\thesis\\figures\\timings\\samples.data";
        BufferedReader reader = new BufferedReader(new FileReader(path));
        int lines = (int)Files.lines(Paths.get(path)).count();
        int k = Math.min((int)Files.lines(Paths.get(path)).count(), max);

        long[][] samples = new long[k][100000];

        //Skip lines
        for (int i=0; i<lines-k; i++) {
            reader.readLine();
        }

        String line;
        for (int i=0; (line = reader.readLine()) != null && i < max; i++) {
            int j = 0;
            for(String sample : line.split(" ")) {
                samples[i][j] = Long.valueOf(sample);
                j++;
            }
            assert(j == 100000);
            System.out.println(i+1 + "/" + k);
        }

        return samples;
    }

    private void savePlotData(double[] data, String fileName) throws IOException {
        double empiricalMean = getEmpiricalMean(data);
        double empiricalStandardDeviation = getEmpiricalStandardDeviation(data);

        BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Users\\Jonas\\Desktop\\Bachelorarbeit\\thesis\\figures\\timings\\" + fileName + ".dat"));
        writer.append("points y\n");
        for(double point : data) {
            writer.append(Double.toString(point)).append(" 0\n");
        }
        writer.close();

        writer = new BufferedWriter(new FileWriter("C:\\Users\\Jonas\\Desktop\\Bachelorarbeit\\thesis\\figures\\timings\\" + fileName + "-statistic.dat"));
        writer.append("mean deviation y\n");
        writer.append(Double.toString(empiricalMean)).append(" ").append(Double.toString(empiricalStandardDeviation)).append(" -1\n");
        writer.close();
    }

    private double getEmpiricalMean(double[] data) {
        double sum = 0;
        for(double point : data) {
            sum += point;
        }
        return sum / data.length;
    }

    private double getEmpiricalStandardDeviation(double[] data) {
        double temp = 0;
        double empiricalMean = getEmpiricalMean(data);
        for(double point : data) {
            temp += Math.pow((point - empiricalMean), 2);
        }
        return Math.sqrt(temp / (data.length-1));
    }

    private long measureTiming(byte[] plainBlock) throws OracleException {
        return victimProxy.sendEncryptedPlainBlock(plainBlock).getResponseTime();
    }

    private static long[][] deepCopy(long[][] original) {
        long[][] result = new long[original.length][];
        for (int i = 0; i < original.length; i++) {
            result[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return result;
    }
}
