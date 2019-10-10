package opcua.context;

/**
 * Collection of various default values
 */
public class StaticConfig {
    public static long PROTOCOL_VERSION = 0;
    public static long RECEIVE_BUFFER_SIZE = 1024*1024;
    public static long SEND_BUFFER_SIZE = 1024*1024;
    public static long MAX_MESSAGE_SIZE = 1024*1024;
    public static long MAX_CHUNK_COUNT = 1024;

    public static int TIMEOUT = 10000;
    public static long REQUESTED_LIFETIME = 24*60*60;

    public static int CERT_KEYSIZE = 2048;
    public static String CERT_NAME = "CN=OpcUa-Attacker";
}
