package com.taobao.top.link.handler;

import com.taobao.top.link.EndpointProxy;

public interface ReceiveHandler {
	public void onReceive(byte[] data, int offset, int length, EndpointProxy messageFrom);
}
