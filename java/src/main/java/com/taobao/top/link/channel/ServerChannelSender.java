package com.taobao.top.link.channel;

public interface ServerChannelSender extends ChannelSender {
	public boolean isOpen();
	public void close(String reason);
	public Object getContext(Object key);
	public void setContext(Object key, Object value);
}