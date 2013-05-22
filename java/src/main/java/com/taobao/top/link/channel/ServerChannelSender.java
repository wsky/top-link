package com.taobao.top.link.channel;

public interface ServerChannelSender extends ChannelSender {
	public boolean isOpen();
	public void close(int statusCode, String reasonText);
}