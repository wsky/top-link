package com.taobao.top.link.endpoint;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.Identity;
import com.taobao.top.link.Logger;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ChannelHandler;
import com.taobao.top.link.channel.ClientChannel;
import com.taobao.top.link.channel.ClientChannelSelector;
import com.taobao.top.link.channel.ServerChannel;
import com.taobao.top.link.channel.websocket.WebSocketClientChannelSelector;

// just an sample api gateway, upper layer app can use serverChannel/channelSelect directly
// request-reply
public class Endpoint {
	private Logger logger;
	private Identity identity;
	private List<ServerChannel> serverChannels;
	private ClientChannelSelector channelSelectHandler;
	private ChannelHandler channelHandler;

	// in/out endpoints
	private List<EndpointProxy> connected;
	private HashMap<String, EndpointProxy> connectedByUri;

	public Endpoint() {
		this(new DefaultLoggerFactory());
	}

	public Endpoint(Identity identity) {
		this(new DefaultLoggerFactory(), identity);
	}

	public Endpoint(LoggerFactory loggerFactory) {
		this(loggerFactory, null);
	}

	public Endpoint(LoggerFactory loggerFactory, Identity identity) {
		this.serverChannels = new ArrayList<ServerChannel>();
		this.connected = new ArrayList<EndpointProxy>();
		this.connectedByUri = new HashMap<String, EndpointProxy>();
		this.logger = loggerFactory.create(this);
		this.identity = identity;
		this.channelSelectHandler = new WebSocketClientChannelSelector(loggerFactory);
	}

	public Identity getIdentity() {
		return this.identity;
	}

	public void setChannelHandler(ChannelHandler handler) {
		this.channelHandler = handler;
		this.setChannelHandler();
	}

	public ChannelHandler getChannelHandler() {
		return this.channelHandler;
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

	public synchronized EndpointProxy getEndpoint(URI uri) throws ChannelException {
		String uriString = uri.toString();
		EndpointProxy e = this.connectedByUri.get(uriString);
		// always clear, cached proxy will have broken channel
		if (e != null) {
			this.connected.remove(e);
		}
		e = new EndpointProxy();
		// always reget channel, make sure it's valid
		ClientChannel channel = this.channelSelectHandler.getChannel(uri, this.identity);
		channel.setChannelHandler(this.channelHandler);
		e.add(channel);
		this.connected.add(e);
		this.connectedByUri.put(uriString, e);
		return e;
	}

	public synchronized EndpointProxy getEndpoint(Identity identity) {
		for (EndpointProxy e : this.connected) {
			if (e.getIdentity() != null &&
					e.getIdentity().equals(identity))
				return e;
		}
		EndpointProxy e = new EndpointProxy();
		e.setIdentity(identity);
		this.connected.add(e);
		if (this.logger.isDebugEnable())
			this.logger.debug("create new EndpointProxy by identity");
		return e;
	}

	private void setChannelHandler() {
		for (ServerChannel channel : this.serverChannels) {
			channel.setChannelHandler(this.channelHandler);
		}
	}
}
