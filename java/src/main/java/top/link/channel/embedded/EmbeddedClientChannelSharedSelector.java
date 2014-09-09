package top.link.channel.embedded;

import java.net.URI;

import top.link.channel.ChannelException;
import top.link.channel.ClientChannel;
import top.link.channel.ClientChannelSharedSelector;

public class EmbeddedClientChannelSharedSelector extends ClientChannelSharedSelector {
	public EmbeddedClientChannelSharedSelector() {
		super();
	}
	
	protected ClientChannel connect(URI uri, int timeout) throws ChannelException {
		return uri.getScheme().equalsIgnoreCase("ws") ||
				uri.getScheme().equalsIgnoreCase("wss") ?
				EmbeddedWebSocketClient.connect(uri, timeout) :
				super.connect(uri, timeout);
	}
}
