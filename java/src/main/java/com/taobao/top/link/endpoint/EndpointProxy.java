package com.taobao.top.link.endpoint;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.taobao.top.link.LinkException;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ChannelSender;
import com.taobao.top.link.channel.ClientChannel;

public class EndpointProxy {
	private Identity identity;
	// known by both side
	private String token;
	private List<ChannelSender> senders;
	private HashMap<String, ClientChannel> clientChannels;

	private Endpoint endpoint;

	protected EndpointProxy(Endpoint endpoint) {
		this.senders = new ArrayList<ChannelSender>();
		this.clientChannels = new HashMap<String, ClientChannel>();
		this.endpoint = endpoint;
	}

	protected void setIdentity(Identity identity) {
		this.identity = identity;
	}

	protected void setToken(String token) {
		this.token = token;
	}

	protected synchronized void add(ChannelSender sender) {
		this.senders.add(sender);
		if (sender instanceof ClientChannel) {
			ClientChannel channel = (ClientChannel) sender;
			this.clientChannels.put(channel.getUri().toString(), channel);
		}
	}

	protected synchronized void remove(ChannelSender sender) {
		this.senders.remove(sender);
		if (sender instanceof ClientChannel) {
			ClientChannel channel = (ClientChannel) sender;
			this.clientChannels.remove(channel.getUri().toString());
		}
	}

	protected synchronized void remove(URI uri) {
		ClientChannel channel = this.clientChannels.remove(uri.toString());
		if (channel != null)
			this.senders.remove(channel);
	}

	public Identity getIdentity() {
		return this.identity;
	}

	public void sendAndWait(HashMap<String, String> message) throws LinkException {
		this.sendAndWait(message, Endpoint.TIMOUTSECOND);
	}

	public void sendAndWait(HashMap<String, String> message, int timeoutSecond) throws LinkException {
		this.endpoint.sendAndWait(this,
				this.getSenders(),
				this.createMessage(message),
				timeoutSecond);
	}

	public void send(HashMap<String, String> message) throws ChannelException {
		this.endpoint.send(this.getSenders(), this.createMessage(message));
	}

	private Message createMessage(HashMap<String, String> message) {
		Message msg = new Message();
		msg.messageType = MessageType.SEND;
		msg.content = message;
		msg.token = this.token;
		return msg;
	}

	private ChannelSender getSenders() throws ChannelException {
		if (this.senders.isEmpty())
			throw new ChannelException("do not have any valid channel to send");
		return this.senders.get(0);
	}
}