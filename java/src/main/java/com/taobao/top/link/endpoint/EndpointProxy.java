package com.taobao.top.link.endpoint;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.taobao.top.link.LinkException;
import com.taobao.top.link.Text;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ChannelSender;
import com.taobao.top.link.channel.ClientChannel;
import com.taobao.top.link.channel.ServerChannelSender;

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

	protected String getToken() {
		return this.token;
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

	public boolean hasValidSender() {
		for (ChannelSender sender : this.senders) {
			if ((sender instanceof ServerChannelSender &&
					((ServerChannelSender) sender).isOpen()) ||
					(sender instanceof ClientChannel &&
					((ClientChannel) sender).isConnected()))
				return true;
		}
		return false;
	}

	public HashMap<String, String> sendAndWait(
			HashMap<String, String> message) throws LinkException {
		return this.sendAndWait(message, Endpoint.TIMOUT);
	}

	public HashMap<String, String> sendAndWait(
			HashMap<String, String> message, int timeout) throws LinkException {
		return this.sendAndWait(null, message, timeout);
	}

	public HashMap<String, String> sendAndWait(ChannelSender sender,
			HashMap<String, String> message, int timeout) throws LinkException {
		return this.endpoint.sendAndWait(this,
				this.getSenders(sender),
				this.createMessage(message),
				timeout);
	}

	public void send(HashMap<String, String> message) throws ChannelException {
		this.send(null, message);
	}

	public void send(ChannelSender sender, HashMap<String, String> message) throws ChannelException {
		this.endpoint.send(this.getSenders(sender), this.createMessage(message));
	}

	private Message createMessage(HashMap<String, String> message) {
		Message msg = new Message();
		msg.messageType = MessageType.SEND;
		msg.content = message;
		msg.token = this.token;
		return msg;
	}

	private ChannelSender getSenders(ChannelSender sender) throws ChannelException {
		if (this.senders.isEmpty())
			throw new ChannelException(Text.E_NO_SENDER);
		if (this.senders.contains(sender))
			return sender;
		return this.senders.get(0);
	}
}