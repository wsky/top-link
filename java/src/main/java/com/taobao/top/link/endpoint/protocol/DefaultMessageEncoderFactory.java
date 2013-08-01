package com.taobao.top.link.endpoint.protocol;

import com.taobao.top.link.endpoint.Message;
import com.taobao.top.link.endpoint.MessageIO.MessageEncoder;

public class DefaultMessageEncoderFactory implements MessageEncoderFactory {
	private MessageEncoder01 encoder01 = new MessageEncoder01();
	private MessageEncoder02 encoder02 = new MessageEncoder02();

	@Override
	public MessageEncoder get(Message message) {
		if (message.protocolVersion == 1)
			return this.encoder01;
		return this.encoder02;
	}
}