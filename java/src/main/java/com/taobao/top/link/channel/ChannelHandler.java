package com.taobao.top.link.channel;

public interface ChannelHandler {
	public void onConnect(ChannelContext context) throws Exception;
	public void onMessage(ChannelContext context) throws Exception;
	public void onError(ChannelContext context) throws Exception;
	public void onClosed(String reason);
}