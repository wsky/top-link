package com.taobao.top.link.endpoint.protocol;

import java.nio.ByteBuffer;

import com.taobao.top.link.endpoint.MessageIO.MessageDecoder;

public interface MessageDecoderFactory {
	public MessageDecoder get(ByteBuffer buffer);
}