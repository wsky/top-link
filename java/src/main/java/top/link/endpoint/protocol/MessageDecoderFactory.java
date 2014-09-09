package top.link.endpoint.protocol;

import java.nio.ByteBuffer;

import top.link.endpoint.MessageIO.MessageDecoder;

public interface MessageDecoderFactory {
	public MessageDecoder get(ByteBuffer buffer);
}