package top.link.remoting.spring;

import java.util.List;
import java.util.Map.Entry;

import top.link.channel.ChannelContext;
import top.link.channel.ServerChannelSender;
import top.link.remoting.DefaultRemotingServerChannelHandler;

public class SpringRemotingServerChannelHandler extends DefaultRemotingServerChannelHandler {
	private HandshakerBean handshaker;

	public SpringRemotingServerChannelHandler(HandshakerBean handshaker) {
		super();
		this.handshaker = handshaker;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onConnect(ChannelContext context) throws Exception {
		if (this.handshaker == null)
			return;
		this.handshaker.onHandshake(
				(List<Entry<String, String>>) context.getMessage(),
				new Context((ServerChannelSender) context.getSender()));
	}

	public class Context implements ChannelContextBean {
		private ServerChannelSender sender;

		public Context(ServerChannelSender sender) {
			this.sender = sender;
		}

		public Object get(Object key) {
			return this.sender.getContext(key);
		}

		public void set(Object key, Object value) {
			this.sender.setContext(key, value);
		}

	}
}