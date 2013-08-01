package com.taobao.top.link.endpoint.protocol;

import com.taobao.top.link.endpoint.Message;
import com.taobao.top.link.endpoint.MessageIO.MessageEncoder;

public interface MessageEncoderFactory {
	public MessageEncoder get(Message message);
}