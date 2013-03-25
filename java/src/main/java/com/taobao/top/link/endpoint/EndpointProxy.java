package com.taobao.top.link.endpoint;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ChannelSender;
import com.taobao.top.link.channel.ClientChannel;

public class EndpointProxy {
	private Identity identity;
	private List<ChannelSender> senders;
	private HashMap<String, ClientChannel> clientChannels;

	protected EndpointProxy() {
		this.senders = new ArrayList<ChannelSender>();
		this.clientChannels = new HashMap<String, ClientChannel>();
	}

	public Identity getIdentity() {
		return this.identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	public synchronized void add(ChannelSender sender) {
		this.senders.add(sender);
		if (sender instanceof ClientChannel) {
			ClientChannel channel = (ClientChannel) sender;
			this.clientChannels.put(channel.getUri().toString(), channel);
		}
	}

	public synchronized void remove(ChannelSender sender) {
		this.senders.remove(sender);
		if (sender instanceof ClientChannel) {
			ClientChannel channel = (ClientChannel) sender;
			this.clientChannels.remove(channel.getUri().toString());
		}
	}

	public synchronized void remove(URI uri) {
		ClientChannel channel = this.clientChannels.remove(uri.toString());
		if (channel != null)
			this.senders.remove(channel);
	}

	public void send(byte[] data, int offset, int length) throws ChannelException {
		this.checkSenders().get(0).send(data, offset, length);
	}

	public void send(ByteBuffer dataBuffer) throws ChannelException {
		this.checkSenders().get(0).send(dataBuffer, null);
	}

	private List<ChannelSender> checkSenders() throws ChannelException {
		if (this.senders.isEmpty())
			throw new ChannelException("do not have any valid channel to send");
		return this.senders;
	}
}