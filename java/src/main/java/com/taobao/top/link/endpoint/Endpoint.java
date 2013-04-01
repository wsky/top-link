package com.taobao.top.link.endpoint;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.LinkException;
import com.taobao.top.link.Logger;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ChannelSender;
import com.taobao.top.link.channel.ClientChannel;
import com.taobao.top.link.channel.ClientChannelSelector;
import com.taobao.top.link.channel.ServerChannel;

// Abstract network model
// https://docs.google.com/drawings/d/1PRfzMVNGE4NKkpD9A_-QlH2PV47MFumZX8LbCwhzpQg/edit
public class Endpoint {
	protected static int TIMOUTSECOND = 5;
	private Logger logger;
	private Identity identity;
	private List<ServerChannel> serverChannels;
	private ClientChannelSelector channelSelector;
	private EndpointChannelHandler channelHandler;
	private MessageHandler messageHandler;

	// in/out endpoints
	private List<EndpointProxy> connected;

	public Endpoint(Identity identity) {
		this(new DefaultLoggerFactory(), identity);
	}

	public Endpoint(LoggerFactory loggerFactory, Identity identity) {
		this.serverChannels = new ArrayList<ServerChannel>();
		this.connected = new ArrayList<EndpointProxy>();
		this.logger = loggerFactory.create(this);
		this.identity = identity;
		this.channelSelector = new ClientChannelSharedSelector(loggerFactory);
		this.channelHandler = new EndpointChannelHandler(loggerFactory, this);

		if (this.identity == null)
			throw new NullPointerException("identity");
	}

	public Identity getIdentity() {
		return this.identity;
	}

	public void setMessageHandler(MessageHandler handler) {
		this.messageHandler = handler;
	}

	public MessageHandler getMessageHandler() {
		return this.messageHandler;
	}

	public void bind(ServerChannel channel) {
		channel.setChannelHandler(this.channelHandler);
		channel.run();
		this.serverChannels.add(channel);
	}

	public void unbindAll() {
		for (ServerChannel channel : this.serverChannels) {
			try {
				channel.stop();
			} catch (Exception e) {
				this.logger.error("unbind error", e);
			}
		}
		this.serverChannels.clear();
	}

	public Iterator<EndpointProxy> getConnected() {
		return this.connected.iterator();
	}

	public synchronized EndpointProxy getEndpoint(Identity targetIdentity, URI uri) throws LinkException {
		EndpointProxy e = this.getEndpoint(targetIdentity);
		// always clear, cached proxy will have broken channel
		e.remove(uri);
		// always reget channel, make sure it's valid
		ClientChannel channel = this.channelSelector.getChannel(uri);
		channel.setChannelHandler(this.channelHandler);
		e.add(channel);
		// connect message
		Message msg = new Message();
		msg.messageType = MessageType.CONNECT;
		HashMap<String, String> content = new HashMap<String, String>();
		this.identity.render(content);
		msg.content = content;
		this.sendAndWait(e, channel, msg, TIMOUTSECOND);
		return e;
	}

	public synchronized EndpointProxy getEndpoint(Identity identity) throws LinkException {
		if (identity.equals(this.identity))
			throw new LinkException("target identity can not equal itself");

		for (EndpointProxy e : this.connected) {
			if (e.getIdentity() != null &&
					e.getIdentity().equals(identity))
				return e;
		}
		EndpointProxy e = this.createProxy("by identity|" + identity.toString());
		e.setIdentity(identity);
		return e;
	}

	protected void send(ChannelSender sender, Message message) throws ChannelException {
		this.channelHandler.pending(message, sender);
	}

	protected HashMap<String, String> sendAndWait(EndpointProxy e,
			ChannelSender sender,
			Message message,
			int timeoutSecond) throws LinkException {
		SendCallback callback = new SendCallback(e);
		this.channelHandler.pending(message, sender, callback);
		callback.waitReturn(timeoutSecond);
		if (callback.getError() != null) {
			throw callback.getError();
		}
		return callback.getReturn();
	}

	private EndpointProxy createProxy(String reason) {
		EndpointProxy e = new EndpointProxy(this);
		this.connected.add(e);
		if (this.logger.isDebugEnable())
			this.logger.debug("create new EndpointProxy: " + reason);
		return e;
	}
}
