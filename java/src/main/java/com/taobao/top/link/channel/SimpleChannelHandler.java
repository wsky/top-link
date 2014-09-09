package com.taobao.top.link.channel;

public abstract class SimpleChannelHandler implements ChannelHandler {
	public void onConnect(ChannelContext context) throws Exception {

	}
	
	public void onError(ChannelContext context) throws Exception {
	}
	
	public void onClosed(String reason) {	
	}
}
