package com.taobao.top.link.endpoint;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ChannelSender;

public class EndpointProxy {
	private Identity identity;
	private List<ChannelSender> senders;

	protected EndpointProxy() {
		this.senders = new ArrayList<ChannelSender>();
	}

	public Identity getIdentity() {
		return this.identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	public synchronized void add(ChannelSender sender) {
		this.senders.add(sender);
	}

	public synchronized void remove(ChannelSender sender) {
		this.senders.remove(sender);
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