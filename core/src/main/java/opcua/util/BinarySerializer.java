package opcua.util;

import jdk.jshell.spi.ExecutionControl;
import opcua.model.type.NodeId;

import java.io.ByteArrayOutputStream;
import java.security.InvalidParameterException;
import java.time.Instant;
import java.util.Enumeration;

public class BinarySerializer {

    private ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

    public byte[] get() {
        return byteStream.toByteArray();
    }

    public BinarySerializer putBytes(byte[] input) {
        byteStream.writeBytes(input);
        return this;
    }

    public BinarySerializer putByte(byte input) {
        byteStream.write(input);
        return this;
    }

    public BinarySerializer putBoolean(boolean input) {
        byteStream.writeBytes(DataTypeConverter.booleanToBytes(input));
        return this;
    }

    public BinarySerializer putInt32(int input) {
        byteStream.writeBytes(DataTypeConverter.int32ToBytesLE(input));
        return this;
    }

    public BinarySerializer putInt64(long input) {
        byteStream.writeBytes(DataTypeConverter.int64ToByteLe(input));
        return this;
    }

    public BinarySerializer putUInt16(int input) {
        byteStream.writeBytes(DataTypeConverter.uInt16ToBytesLE(input));
        return this;
    }

    public BinarySerializer putUInt32(long input) {
        byteStream.writeBytes(DataTypeConverter.uInt32ToBytesLE(input));
        return this;
    }

    public BinarySerializer putString(String input) {
        if(input == null) {
            putInt32(-1);
        } else {
            putInt32(input.length());
            putBytes(input.getBytes());
        }
        return this;
    }

    /**
     * Serializing NodeId as described in TODO 6.5.2.2.8
     */
    public BinarySerializer putNodeId(NodeId input) {
        if(input == null) {
            input = NodeId.NUMERIC_NULL;
        }

        int namespace = input.getNamespace();
        if(input.getType() == NodeId.Type.NUMERIC) {
            long value = (long)input.getValue();

            if(value < 256 && namespace == 0) {    //We can use the 2-byte representation
                putByte(NodeId.DataEncoding.TWO_BYTE.getFlag());
                putByte((byte)value);
            }
            else if(value < 256*256 && namespace < 256) {    //We can use the 4-byte representation
                putByte(NodeId.DataEncoding.FOUR_BYTE.getFlag());
                putByte((byte) namespace);
                putUInt16((int) value);
            }
            else {      //We must use the normal numeric representation :-(
                putByte(NodeId.DataEncoding.NUMERIC.getFlag());
                putUInt16(namespace);
                putUInt32(value);
            }
        } else {
            throw new UnsupportedOperationException();
        }

        return this;
    }

    /**
     * Serializing timestamps as described in TODO 6.5.2.2.5
     */
    public BinarySerializer putDateTime(Instant input) {
        if(input == null) {
            putInt64(0);
            return this;
        }
        try {
            putInt64(input.toEpochMilli());
        } catch (ArithmeticException e) {
            putInt64(0);
        }
        return this;
    }

    public BinarySerializer putEnumeration(Enum input) {
        if(input == null) {
            throw new InvalidParameterException("Null-Enumarations cannot be encoded");
        }
        putInt32(input.ordinal());
        return this;
    }

    public BinarySerializer putByteArray(byte[] input) {
        if(input == null) {
            putInt32(-1);
        } else {
            putInt32(input.length);
            putBytes(input);
        }
        return this;
    }

}
