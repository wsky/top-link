package top.link.channel;

import java.net.URI;
import java.util.Hashtable;

import top.link.Pool;
import top.link.Text;
import top.link.channel.websocket.WebSocketClient;

public class ClientChannelPooledSelector implements ClientChannelSelector {
	private final static int CONNECT_TIMEOUT = 5000;
	private Hashtable<String, Pool<ClientChannel>> channels;
	private Object lockObject;
	
	public ClientChannelPooledSelector() {
		this.channels = new Hashtable<String, Pool<ClientChannel>>();
		this.lockObject = new Object();
	}
	
	public ClientChannel getChannel(final URI uri) throws ChannelException {
		String url = uri.toString();
		if (this.channels.get(url) == null) {
			synchronized (this.lockObject) {
				if (this.channels.get(url) == null) {
					this.channels.put(url,
							this.createChannelPool(uri, CONNECT_TIMEOUT));
				}
			}
		}
		
		try {
			return this.channels.get(url).chekOut();
		} catch (ChannelException e) {
			throw e;
		} catch (Throwable e) {
			throw new ChannelException(Text.GET_CHANNEL_ERROR, e);
		}
	}
	
	public void returnChannel(ClientChannel channel) {
		this.channels.get(channel.getUri().toString()).checkIn(channel);
	}
	
	protected ChannelPool createChannelPool(URI uri, int timeout) {
		return new ChannelPool(uri, timeout);
	}
	
	public class ChannelPool extends Pool<ClientChannel> {
		protected URI uri;
		protected int timeout;
		
		public ChannelPool(URI uri, int timeout) {
			super(50, 10);
			this.uri = uri;
			this.timeout = timeout;
		}
		
		@Override
		public ClientChannel chekOut() throws Throwable {
			ClientChannel channel = super.chekOut();
			if (channel == null)
				throw new ChannelException(Text.RPC_POOL_BUSY);
			return channel;
		}
		
		@Override
		public ClientChannel create() throws ChannelException {
			return WebSocketClient.connect(this.uri, this.timeout);
		}
		
		@Override
		public boolean validate(ClientChannel t) {
			return t.isConnected();
		}
		
	}
}
