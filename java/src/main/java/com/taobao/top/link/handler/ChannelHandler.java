package com.taobao.top.link.handler;

import java.nio.ByteBuffer;

import com.taobao.top.link.EndpointContext;

public abstract class ChannelHandler {
	public abstract void onReceive(ByteBuffer dataBuffer, EndpointContext context);
	public abstract void onException(Throwable exception);
}
