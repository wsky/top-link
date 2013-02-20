package com.taobao.top.link;

public abstract class EndpointContext {
	public abstract void reply(byte[] data, int offset, int length);
}
