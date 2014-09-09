package top.link.channel.embedded;

import java.net.URI;

import top.link.channel.ChannelException;
import top.link.channel.ClientChannel;
import top.link.channel.ClientChannelPooledSelector;

public class EmbeddedClientChannelPooledSelector extends ClientChannelPooledSelector {
	public EmbeddedClientChannelPooledSelector() {
		super();
	}
	
	protected ChannelPool createChannelPool(URI uri, int timeout) {
		return new ChannelPool(uri, timeout);
	}
	
	public class EmbeddedChannelPool extends ChannelPool {
		public EmbeddedChannelPool(URI uri, int timeout) {
			super(uri, timeout);
		}
		
		@Override
		public ClientChannel create() throws ChannelException {
			return uri.getScheme().equalsIgnoreCase("ws") ||
					uri.getScheme().equalsIgnoreCase("wss") ?
					EmbeddedWebSocketClient.connect(this.uri, this.timeout) :
					super.create();
		}
	}
}