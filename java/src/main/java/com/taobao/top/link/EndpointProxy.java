package com.taobao.top.link;

import java.nio.ByteBuffer;

import com.taobao.top.link.handler.ChannelHandler;
import com.taobao.top.link.handler.SimpleChannelHandler;

public class EndpointProxy {
	private ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 1024);
	private ClientChannel channel;

	protected void using(ClientChannel channel) {
		this.channel = channel;
	}

	public void send(byte[] data, int offset, int length) throws ChannelException {
		this.channel.send(data, offset, length);
	}

	// special once-handle
	public void send(byte[] data, int offset, int length, ChannelHandler handler) {

	}

	// like sync RPC, but do not guarantee timing, just a sample
	public byte[] call(byte[] data, int offset, int length) throws ChannelException {
		final String sync = new String("call");
		// FIXME:if error, remove this once-handler
		this.channel.addOnceChannelHandler(new SimpleChannelHandler() {
			@Override
			public void onReceive(byte[] data, int offset, int length, EndpointContext context) {
				buffer.position(0);
				buffer.put(data, offset, length);
				synchronized (sync) {
					sync.notify();
				}
			}
		});
		this.channel.send(data, offset, length);
		synchronized (sync) {
			try {
				sync.wait(5000);
			} catch (InterruptedException e) {
				throw new ChannelException("rpc call time out", e);
			}
		}
		byte[] result = new byte[this.buffer.position()];
		this.buffer.position(0);
		this.buffer.get(result);
		return result;
	}
}
