package com.taobao.top.link.remoting;

import java.net.URI;
import java.util.Hashtable;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.Pool;
import com.taobao.top.link.Text;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ClientChannel;
import com.taobao.top.link.channel.websocket.WebSocketClient;
import com.taobao.top.link.endpoint.ClientChannelSharedSelector;

public class ClientChannelPooledSelector extends ClientChannelSharedSelector {
	private Hashtable<String, Pool<ClientChannel>> channels;

	public ClientChannelPooledSelector() {
		this(DefaultLoggerFactory.getDefault());
	}

	public ClientChannelPooledSelector(LoggerFactory factory) {
		super(factory);
		this.channels = new Hashtable<String, Pool<ClientChannel>>();
	}

	@Override
	public ClientChannel getChannel(final URI uri) throws ChannelException {
		String url = uri.toString();
		if (this.channels.get(url) == null) {
			synchronized (this.lockObject) {
				if (this.channels.get(url) == null) {
					this.channels.put(url, new ChannelPool(uri, this.loggerFactory));
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
		private LoggerFactory loggerFactory;

		public ChannelPool(URI uri, LoggerFactory loggerFactory) {
			super(10, 10);
			this.uri = uri;
			this.loggerFactory = loggerFactory;
		}

		public ClientChannel checkout() throws Throwable {
			ClientChannel channel = super.chekOut();
			if (channel == null)
				throw new ChannelException(Text.RPC_POOL_BUSY);
			return channel;
		}

		@Override
		public ClientChannel create() throws ChannelException {
			return WebSocketClient.connect(this.loggerFactory, this.uri, 5000);
		}

		@Override
		public boolean validate(ClientChannel t) {
			return t.isConnected();
		}

	}
}
