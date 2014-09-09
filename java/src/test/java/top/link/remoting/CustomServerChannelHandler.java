package top.link.remoting;

import java.util.List;
import java.util.Map.Entry;

import top.link.LinkException;
import top.link.channel.ChannelContext;
import top.link.remoting.DefaultRemotingServerChannelHandler;

public class CustomServerChannelHandler extends DefaultRemotingServerChannelHandler {
	public final static String ID = "id";

	@SuppressWarnings("unchecked")
	public void onConnect(ChannelContext context) throws LinkException {
		Object msg = context.getMessage();
		if (msg instanceof List<?>) {
			List<Entry<String, String>> headers = (List<Entry<String, String>>) msg;
			for (Entry<String, String> entry : headers) {
				if (entry.getKey().equalsIgnoreCase(ID))
					return;
			}
		}
		throw new LinkException("miss id, 不支持");
	}
}
