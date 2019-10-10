package opcua.message.parts;

import opcua.encoding.EncodingException;

public interface SecurityHeader {
    byte[] toBinary() throws EncodingException;
}
