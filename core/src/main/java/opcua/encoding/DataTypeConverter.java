package opcua.encoding;

import java.nio.charset.Charset;

/**
 * Utility functions for data type conversions
 */
public class DataTypeConverter {

    public static byte[] booleanToBytes(boolean b) {
        byte[] bytes = new byte[1];
        if(b) {
            bytes[0] = 0x1;
        }
        return bytes;
    }

    public static boolean bytesToBoolean(byte[] bytes) {
        return bytes[0] != 0x0;
    }

    public static byte[] int32ToBytesLE(int i) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte)i;
        bytes[1] = (byte)(i >> 8);
        bytes[2] = (byte)(i >> 16);
        bytes[3] = (byte)(i >> 24);
        return bytes;
    }

    public static int bytesToInt32LE(byte[] bytes) {
        return (bytes[0] & 0xFF) | (bytes[1] & 0xFF) << 8 | (bytes[2] & 0xFF) << 16 | bytes[3] << 24;
    }

    public static byte[] int64ToByteLe(long l) {
        byte[] bytes = new byte[8];
        bytes[0] = (byte)l;
        bytes[1] = (byte)(l >> 8);
        bytes[2] = (byte)(l >> 16);
        bytes[3] = (byte)(l >> 24);
        bytes[4] = (byte)(l >> 32);
        bytes[5] = (byte)(l >> 40);
        bytes[6] = (byte)(l >> 48);
        bytes[7] = (byte)(l >> 56);
        return bytes;
    }

    public static long bytesToInt64LE(byte[] bytes) {
        return (bytes[0] & 0xFF) | (bytes[1] & 0xFF) << 8 | (bytes[2] & 0xFF) << 16 | (bytes[3] & 0xFF) << 24 |
                (bytes[4] & 0xFF) << 32 | (bytes[5] & 0xFF) << 40 | (bytes[6] & 0xFF) << 48 | bytes[7] << 56;
    }

    public static byte[] uInt32ToBytesLE(long l) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte)l;
        bytes[1] = (byte)(l >> 8);
        bytes[2] = (byte)(l >> 16);
        bytes[3] = (byte)(l >> 24);
        return bytes;
    }

    public static byte[] uInt32ToBytesBE(long l) {
        byte[] bytes = new byte[4];
        bytes[3] = (byte)l;
        bytes[2] = (byte)(l >> 8);
        bytes[1] = (byte)(l >> 16);
        bytes[0] = (byte)(l >> 24);
        return bytes;
    }

    public static long bytesToUInt32LE(byte[] bytes) {
        long l = 0;
        //Need 0xFF mask for unsigned interpretation
        l += (long)(bytes[0] & 0xFF);
        l += (long)(bytes[1] & 0xFF) << 8;
        l += (long)(bytes[2] & 0xFF) << 16;
        l += (long)(bytes[3] & 0xFF) << 24;
        return l;
    }

    public static byte[] uInt16ToBytesLE(int i) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte)i;
        bytes[1] = (byte)(i >> 8);
        return bytes;
    }

    public static int bytesToUInt16LE(byte[] bytes) {
        int i = 0;
        //Need 0xFF mask for unsigned interpretation
        i += bytes[0] & 0xFF;
        i += (bytes[1] & 0xFF) << 8;
        return i;
    }

    public static byte[] stringToBytes(String s) {
        if(s == null) {
            return int32ToBytesLE(-1);
        }

        byte[] strBytes = s.getBytes(Charset.forName("UTF-8"));
        byte[] result = new byte[strBytes.length + 4];
        System.arraycopy(int32ToBytesLE(strBytes.length), 0, result, 0, 4);
        System.arraycopy(strBytes, 0, result, 4, strBytes.length);
        return result;
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHexString(byte[] bytes) {
        if(bytes == null) {
            return "";
        }
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
