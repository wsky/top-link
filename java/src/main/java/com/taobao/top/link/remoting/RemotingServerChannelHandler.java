package com.taobao.top.link.remoting;

import java.nio.ByteBuffer;

import com.taobao.top.link.BufferManager;
import com.taobao.top.link.ClientChannel.SendHandler;
import com.taobao.top.link.EndpointContext;
import com.taobao.top.link.handler.SimpleChannelHandler;

public abstract class RemotingServerChannelHandler extends SimpleChannelHandler {
	@Override
	public void onReceive(ByteBuffer dataBuffer, EndpointContext context) {
		//TODO:resolve request by sink
		
		int flag = dataBuffer.getInt();
		final ByteBuffer responseBuffer = BufferManager.getBuffer();
		responseBuffer.putInt(flag);

		// remoting message mode:
		// - one-way
		// - two-way
		// - request
		this.onRequest(dataBuffer, responseBuffer);
		
		responseBuffer.flip();
		context.reply(responseBuffer, new SendHandler() {
			@Override
			public void onSendComplete() {
				BufferManager.returnBuffer(responseBuffer);
			}
		});
	}

	public abstract void onRequest(ByteBuffer requestBuffer, ByteBuffer responseBuffer);
}
