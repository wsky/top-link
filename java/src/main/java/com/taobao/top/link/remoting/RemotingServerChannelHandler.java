package com.taobao.top.link.remoting;

import java.nio.ByteBuffer;

import com.taobao.top.link.BufferManager;
import com.taobao.top.link.EndpointContext;
import com.taobao.top.link.handler.SimpleChannelHandler;

public abstract class RemotingServerChannelHandler extends SimpleChannelHandler {
	@Override
	public void onReceive(ByteBuffer dataBuffer, EndpointContext context) {
		int flag = dataBuffer.getInt();
		ByteBuffer responseBuffer = BufferManager.getBuffer();
		responseBuffer.putInt(flag);

		this.onRequest(dataBuffer, responseBuffer);
		responseBuffer.flip();
		context.reply(responseBuffer);
	}

	public abstract void onRequest(ByteBuffer requestBuffer, ByteBuffer responseBuffer);
}
