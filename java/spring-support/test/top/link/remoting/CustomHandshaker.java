package top.link.remoting;

import java.util.List;
import java.util.Map.Entry;

import top.link.LinkException;
import top.link.remoting.ChannelContextBean;
import top.link.remoting.HandshakerBean;

public class CustomHandshaker implements HandshakerBean {
	public void onHandshake(List<Entry<String, String>> headers, ChannelContextBean context) throws Exception {
		for (Entry<String, String> entry : headers) {
			// custom your context for this connection
			context.set(entry.getKey(), entry.getValue());
			// validate something
			if (entry.getKey().equalsIgnoreCase("id"))
				return;
		}
		throw new LinkException("miss id, 不支持");
	}
}
