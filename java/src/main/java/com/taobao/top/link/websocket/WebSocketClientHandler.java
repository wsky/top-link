package com.taobao.top.link.websocket;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpHeaders.Values;
import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;

import com.taobao.top.link.EndpointContext;
import com.taobao.top.link.handler.ChannelHandler;

// one handler per connection
public class WebSocketClientHandler extends SimpleChannelUpstreamHandler {
	protected ChannelHandler channelHandler;
	protected WebSocketClientHandshaker handshaker;
	protected ChannelFuture handshakeFuture;

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Object msg = e.getMessage();
		if (!this.handshaker.isHandshakeComplete()) {
			HttpResponse response = (HttpResponse) e.getMessage();
			HttpResponseStatus status = new HttpResponseStatus(101, "Web Socket Protocol Handshake");
			boolean validStatus = response.getStatus().equals(status);
			boolean validUpgrade = response.getHeader(Names.UPGRADE).equalsIgnoreCase(Values.WEBSOCKET);
			boolean validConnection = response.getHeader(Names.CONNECTION).equalsIgnoreCase(Values.UPGRADE);

			if (!validStatus || !validUpgrade || !validConnection) {
				this.handshakeFuture.setFailure(new Exception("Invalid handshake response"));
			} else {
				this.handshaker.finishHandshake(ctx.getChannel(), response);
				this.handshakeFuture.setSuccess();
			}

			synchronized (this.handshaker) {
				this.handshaker.notify();
			}

			return;
		}

		if (msg instanceof WebSocketFrame) {
			this.handleWebSocketFrame(ctx, (WebSocketFrame) msg);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		if (this.handshakeFuture != null && !this.handshakeFuture.isDone()) {
			this.handshakeFuture.setFailure(e.getCause());
			synchronized (this.handshaker) {
				this.handshaker.notify();
			}
		}
	}

	private void handleWebSocketFrame(final ChannelHandlerContext ctx, WebSocketFrame frame) {
		if (frame instanceof CloseWebSocketFrame) {
			ctx.getChannel().close();
		} else if (frame instanceof BinaryWebSocketFrame) {
			ChannelBuffer buffer = ((BinaryWebSocketFrame) frame).getBinaryData();
			if (this.channelHandler != null) {
				this.channelHandler.onReceive(buffer.array(),
						buffer.arrayOffset(),
						buffer.capacity(),
						new EndpointContext() {
							@Override
							public void reply(byte[] data, int offset, int length) {
								ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(data, offset, length);
								BinaryWebSocketFrame frame = new BinaryWebSocketFrame(buffer);
								frame.setFinalFragment(true);
								ctx.getChannel().write(frame);
							}
						});
			}

		}

	}
}