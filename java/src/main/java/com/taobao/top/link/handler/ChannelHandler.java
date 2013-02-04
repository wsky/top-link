package com.taobao.top.link.handler;

import com.taobao.top.link.EndpointProxy;
import com.taobao.top.link.Identity;

public interface ChannelHandler {
	
	public Identity receiveHandshake(byte[] data, int offset, int length);
	
	public void onReceive(byte[] data, int offset, int length, EndpointProxy messageFrom);
}
