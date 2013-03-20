package com.taobao.top.link;

import com.taobao.top.link.handler.ChannelHandler;

public interface ClientChannel extends ChannelSender {
	public  boolean isConnected();
	public  void setChannelHandler(ChannelHandler handler);
}
