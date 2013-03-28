package com.taobao.top.link.endpoint;

import java.util.HashMap;

import com.taobao.top.link.LinkException;
import com.taobao.top.link.channel.ChannelContext;

public class EndpointContext {
	private ChannelContext channelContext;
	private HashMap<String, String> message;
	private Identity messageFrom;
	private String token;
	private Endpoint endpoint;

	public EndpointContext(ChannelContext channelContext, Endpoint endpoint, Identity messageFrom, String token) {
		this.channelContext = channelContext;
		this.endpoint = endpoint;
		this.messageFrom = messageFrom;
		this.token = token;
	}

	protected void setMessage(HashMap<String, String> message) {
		this.message = message;
	}

	public Identity getMessageFrom() {
		return this.messageFrom;
	}

	public HashMap<String, String> getMessage() {
		return this.message;
	}

	public void reply(HashMap<String, String> message) throws LinkException {
		this.endpoint.send(this.channelContext.getSender(), this.createMessage(message));
	}

	private Message createMessage(HashMap<String, String> message) {
		Message msg = new Message();
		msg.messageType = MessageType.SEND;
		msg.content = message;
		msg.token = this.token;
		return msg;
	}
}
