package com.taobao.top.link;

import java.nio.ByteBuffer;

public abstract class EndpointContext {
	public abstract void reply(byte[] data, int offset, int length);
	public abstract void reply(ByteBuffer dataBuffer);
}
