package com.taobao.top.link.endpoint;

import java.util.Map;

import com.taobao.top.link.LinkException;
import com.taobao.top.link.channel.ChannelContext;

public class EndpointContext {
	private ChannelContext channelContext;
	private Map<String, String> message;
	private Identity messageFrom;
	private int flag;
	private String token;
	private Endpoint endpoint;

	public EndpointContext(ChannelContext channelContext,
			Endpoint endpoint,
			Identity messageFrom,
			int flag,
			String token) {
		this.channelContext = channelContext;
		this.endpoint = endpoint;
		this.messageFrom = messageFrom;
		this.flag = flag;
		this.token = token;
	}

	protected void setMessage(Map<String, String> message) {
		this.message = message;
	}

	public Identity getMessageFrom() {
		return this.messageFrom;
	}

	public Map<String, String> getMessage() {
		return this.message;
	}

	public void reply(Map<String, String> message) throws LinkException {
		this.endpoint.send(this.channelContext.getSender(), this.createMessage(message));
	}

	public void error(int statusCode, String statusPhase) throws LinkException {
		Message msg = this.createMessage(null);
		msg.statusCode = statusCode;
		msg.statusPhase = statusPhase;
		this.endpoint.send(this.channelContext.getSender(), msg);
	}

	private Message createMessage(Map<String, String> message) {
		Message msg = new Message();
		msg.messageType = MessageType.SENDACK;
		msg.content = message;
		msg.flag = this.flag;
		msg.token = this.token;
		return msg;
	}
}
