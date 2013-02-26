package com.taobao.top.link.remoting;

import java.nio.ByteBuffer;

import com.taobao.top.link.EndpointContext;
import com.taobao.top.link.handler.SimpleChannelHandler;

public abstract class RemotingServerChannelHandler extends SimpleChannelHandler {
	@Override
	public void onReceive(byte[] data, int offset, int length, EndpointContext context) {
		ByteBuffer request = ByteBuffer.wrap(data, offset, length);
		int flag = request.getInt();

		//System.out.println(String.format("receive request of rpc-call#%s", flag));
		
		// upper-layer logic
		byte[] result = this.onRequest(request);

		ByteBuffer reply = ByteBuffer.wrap(new byte[length + 4]);
		reply.putInt(flag);
		reply.put(result, 0, result.length);
		context.reply(reply.array(), reply.arrayOffset(), reply.capacity());

		// this.onRequest(buffer, new EndpointContext() {
		// @Override
		// public void reply(byte[] data, int offset, int length) {
		// ByteBuffer buffer = ByteBuffer.wrap(new byte[length + 4]);
		// buffer.putInt(flag);
		// buffer.put(data, offset, length);
		// context.reply(buffer.array(), buffer.arrayOffset(),
		// buffer.capacity());
		// }
		// });
	}

	public abstract byte[] onRequest(ByteBuffer buffer);
}
