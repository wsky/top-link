package com.taobao.top.link.channel;

public abstract class SimpleChannelHandler implements ChannelHandler {
	@Override
	public void onConnect(ChannelContext context) throws Exception {

	}
	@Override
	public void onError(ChannelContext context) throws Exception {
	}
	
	@Override
	public void onClosed(String reason) {	
	}
}
