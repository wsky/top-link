package com.taobao.top.link.websocket;

import java.net.URI;
import java.util.Hashtable;

import com.taobao.top.link.ChannelException;
import com.taobao.top.link.ClientChannel;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.Pool;

public class WebSocketClientChannelPooledSelector extends WebSocketClientChannelSelector {
	private Hashtable<String, Pool<ClientChannel>> channels;

	public WebSocketClientChannelPooledSelector(LoggerFactory factory) {
		super(factory);
		this.channels = new Hashtable<String, Pool<ClientChannel>>();
	}

	@Override
	public ClientChannel getChannel(final URI uri) throws ChannelException {
		String url = uri.toString();
		if (this.channels.get(url) == null) {
			synchronized (this.lockObject) {
				if (this.channels.get(url) == null) {
					this.channels.put(url, new ChannelPool(uri, this));
				}
			}
		}

		try {
			return this.channels.get(url).chekOut();
		} catch (Throwable e) {
			throw (ChannelException) e;
		}
	}

	@Override
	public void returnChannel(ClientChannel channel) {
		this.channels.get(channel.getUri().toString()).checkIn(channel);
	}

	class ChannelPool extends Pool<ClientChannel> {
		private URI uri;
		private WebSocketClientChannelPooledSelector selector;

		public ChannelPool(URI uri, WebSocketClientChannelPooledSelector selector) {
			super(10, 10);
			this.uri = uri;
			this.selector = selector;
		}

		public ClientChannel checkout() throws Throwable {
			ClientChannel channel = super.chekOut();
			if (channel == null)
				throw new ChannelException("channel pool is busy, retry later");
			return channel;
		}

		@Override
		public ClientChannel create() throws ChannelException {
			return this.selector.connect(this.uri, 5000, null);
		}

		@Override
		public boolean validate(ClientChannel t) {
			return t.isConnected();
		}

	}
}
