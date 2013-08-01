package com.taobao.top.link.endpoint.protocol;

import java.nio.ByteBuffer;

import com.taobao.top.link.endpoint.MessageIO.MessageDecoder;

public class DefaultMessageDecoderFactory implements MessageDecoderFactory {
	private MessageDecoder01 decoder01 = new MessageDecoder01();
	private MessageDecoder02 decoder02 = new MessageDecoder02();

	@Override
	public MessageDecoder get(ByteBuffer buffer) {
		int version = buffer.get();
		if (version == 1)
			return this.decoder01;
		return this.decoder02;
	}
}
