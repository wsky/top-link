package com.taobao.top.link.handler;

import com.taobao.top.link.EndpointContext;

public abstract class ChannelHandler {
	public abstract void onReceive(byte[] data, int offset, int length, EndpointContext context);
	public abstract void onException(Throwable exception);
}
