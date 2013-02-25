package com.taobao.top.link.handler;

import com.taobao.top.link.EndpointContext;

public interface ChannelHandler {
	public void onReceive(byte[] data, int offset, int length, EndpointContext context);
}
