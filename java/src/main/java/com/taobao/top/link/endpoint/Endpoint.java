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
import com.taobao.top.link.channel.ClientChannel;
import com.taobao.top.link.channel.ClientChannelSelector;
import com.taobao.top.link.channel.ServerChannel;

// Abstract network model
// https://docs.google.com/drawings/d/1PRfzMVNGE4NKkpD9A_-QlH2PV47MFumZX8LbCwhzpQg/edit
public class Endpoint {
	private Logger logger;
	private Identity identity;
	private List<ServerChannel> serverChannels;
	private ClientChannelSelector channelSelector;
	private EndpointChannelHandler channelHandler;
	private MessageHandler messageHandler;

	// in/out endpoints
	private List<EndpointProxy> connected;
	private HashMap<String, EndpointProxy> connectedByUri;

	public Endpoint(Identity identity) {
		this(new DefaultLoggerFactory(), identity);
	}

	public Endpoint(LoggerFactory loggerFactory, Identity identity) {
		this.serverChannels = new ArrayList<ServerChannel>();
		this.connected = new ArrayList<EndpointProxy>();
		this.connectedByUri = new HashMap<String, EndpointProxy>();
		this.logger = loggerFactory.create(this);
		this.identity = identity;
		this.channelSelector = new ClientChannelSharedSelector(loggerFactory);
		this.channelHandler = new EndpointChannelHandler(loggerFactory, this);
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

	public synchronized EndpointProxy getEndpoint(URI uri) throws LinkException {
		String uriString = uri.toString();
		EndpointProxy e = this.connectedByUri.get(uriString);
		if (e == null)
			e = this.createProxy("by uri " + uriString);
		// always clear, cached proxy will have broken channel
		e.remove(uri);
		// always reget channel, make sure it's valid
		ClientChannel channel = this.channelSelector.getChannel(uri);
		channel.setChannelHandler(this.channelHandler);
		e.add(channel);
		// TODO:send connect
		SendCallback callback = new SendCallback(e);
		this.channelHandler.pending(new Message(), callback);
		callback.waitReturn(5000);
		if (callback.getError() != null) {
			throw callback.getError();
		}
		this.connectedByUri.put(uriString, e);
		return e;
	}

	public synchronized EndpointProxy getEndpoint(Identity identity) {
		for (EndpointProxy e : this.connected) {
			if (e.getIdentity() != null &&
					e.getIdentity().equals(identity))
				return e;
		}
		EndpointProxy e = this.createProxy("by identity|" + identity.toString());
		e.setIdentity(identity);
		return e;
	}

	private EndpointProxy createProxy(String reason) {
		EndpointProxy e = new EndpointProxy();
		this.connected.add(e);
		if (this.logger.isDebugEnable())
			this.logger.debug("create new EndpointProxy: " + reason);
		return e;
	}
}
