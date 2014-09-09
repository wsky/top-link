package top.link.endpoint.protocol;

import top.link.endpoint.Message;
import top.link.endpoint.MessageIO.MessageEncoder;

public interface MessageEncoderFactory {
	public MessageEncoder get(Message message);
}