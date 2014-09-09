package top.link.endpoint;

import top.link.LinkException;
import top.link.Text;
import top.link.channel.ServerChannelSender;

public class SingleProxyStateHandler implements StateHandler {
	public void onConnect(EndpointProxy endpoint, ChannelSenderWrapper sender, Identity connectingIdentity) throws LinkException {
		if (!(sender.getChannelSender() instanceof ServerChannelSender))
			return;
		// FIXME hack here, maybe not alwasy ServerChannelSender
		ServerChannelSender serverSender = (ServerChannelSender) sender.getChannelSender();
		if (serverSender.getContext("__endpoint") != null)
			throw new LinkException(Text.E_SINGLE_ALLOW);
		serverSender.setContext("__endpoint", endpoint);
	}

}
