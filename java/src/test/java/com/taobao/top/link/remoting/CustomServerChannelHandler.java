package com.taobao.top.link.remoting;

import java.util.List;
import java.util.Map.Entry;

import com.taobao.top.link.LinkException;
import com.taobao.top.link.channel.ChannelContext;

public class CustomServerChannelHandler extends DefaultRemotingServerChannelHandler {
	@SuppressWarnings("unchecked")
	public void onConnect(ChannelContext context) throws LinkException {
		Object msg = context.getMessage();
		if (msg instanceof List<?>) {
			List<Entry<String, String>> headers = (List<Entry<String, String>>) msg;
			for (Entry<String, String> entry : headers) {
				if (entry.getKey().equalsIgnoreCase("id"))
					return;
			}
		}
		throw new LinkException("dot not support, 不支持");
	}
}
