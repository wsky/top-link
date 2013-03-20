package com.taobao.top.link;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.taobao.top.link.handler.ChannelHandler;
import com.taobao.top.link.websocket.WebSocketClientChannelSelector;

// just an sample api gateway, upper layer app can use serverChannel/channelSelect directly
// request-reply
public class Endpoint {
	private Identity identity;
	private ServerChannel serverChannel;
	private ClientChannelSelector channelSelectHandler;
	private ChannelHandler channelHandler;

	// in/out endpoints
	private List<EndpointProxy> connected;

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
		this.connected = new ArrayList<EndpointProxy>();
		this.channelSelectHandler = new WebSocketClientChannelSelector(loggerFactory);
		this.identity = identity;
	}

	public Identity getIdentity() {
		return this.identity;
	}

	public void setChannelHandler(ChannelHandler handler) {
		this.channelHandler = handler;
	}
	
	public ChannelHandler getChannelHandler() {
		return this.channelHandler;
	}

	public void bind(ServerChannel channel) {
		this.serverChannel = channel;
		this.serverChannel.run(this);
	}

	public void unbind() {
		if (this.serverChannel != null)
			this.serverChannel.stop();
	}
	
	public Iterator<EndpointProxy> getConnected() {
		return this.connected.iterator();
	}

	public synchronized EndpointProxy getEndpoint(URI uri) throws ChannelException {
		EndpointProxy e = new EndpointProxy();
		ClientChannel channel = this.channelSelectHandler.getClientChannel(uri);
		channel.setChannelHandler(this.channelHandler);
		e.add(channel);
		this.connected.add(e);
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
		return e;
	}
}
