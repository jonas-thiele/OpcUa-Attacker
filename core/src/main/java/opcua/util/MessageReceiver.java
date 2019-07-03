package opcua.util;

import opcua.context.Context;
import opcua.message.Message;
import opcua.message.parts.MessageType;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MessageReceiver {
    private static final Logger LOGGER = Logger.getRootLogger();

    public static Message receiveMessage(Context context) throws IOException {
        MessageType messageType = MessageType.fromIdentifier(context.getConnection().receiveData(3));

        if(messageType.isConnectionProtocolMessage()) {
            //Read header
            context.getConnection().receiveData(1); //Discard reserved byte
            long bodySize = DataTypeConverter.bytesToUInt32LE(context.getConnection().receiveData(4)) - 8;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            //Read body
            if(bodySize > Integer.MAX_VALUE) {   //UInt32 could be up to twice as large as int
                buffer.writeBytes(context.getConnection().receiveData(Integer.MAX_VALUE));
                bodySize = bodySize - Integer.MAX_VALUE;
            }
            buffer.writeBytes(context.getConnection().receiveData((int) bodySize));
            byte[] body = buffer.toByteArray();

            return Message.constructFromBinary(messageType, new MessageInputStream(new ByteArrayInputStream(body)));
        }

        return null; //TODO
    }
}
