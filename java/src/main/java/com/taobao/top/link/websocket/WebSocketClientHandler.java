package com.taobao.top.link.websocket;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

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
import com.taobao.top.link.Logger;
import com.taobao.top.link.handler.ChannelHandler;

// one handler per connection
public class WebSocketClientHandler extends SimpleChannelUpstreamHandler {
	protected Logger logger;

	protected WebSocketClientHandshaker handshaker;
	protected ChannelFuture handshakeFuture;

	protected Queue<ChannelHandler> onceHandlers;
	protected ChannelHandler channelHandler;
	protected ClearHandler clearHandler;

	public WebSocketClientHandler(Logger logger) {
		this.logger = logger;
		this.onceHandlers = new ConcurrentLinkedQueue<ChannelHandler>();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		if (this.handshakeFuture != null && !this.handshakeFuture.isSuccess()) {
			this.handshakeFuture.setFailure(e.getCause());
			this.notifyHandshake();
		} else {
			this.logger.error("exceptionCaught", e.getCause());
			this.channelHandler.onException(e.getCause());
		}
		this.clear(ctx);
	}

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
			}

			this.notifyHandshake();

			return;
		}

		if (msg instanceof WebSocketFrame) {
			this.handleWebSocketFrame(ctx, (WebSocketFrame) msg);
		}
	}

	private void handleWebSocketFrame(final ChannelHandlerContext ctx, WebSocketFrame frame) {
		if (frame instanceof CloseWebSocketFrame) {
			CloseWebSocketFrame closeFrame = (CloseWebSocketFrame) frame;
			this.clear(ctx);
			this.logger.warn("connection closed: %s|%s",
					closeFrame.getStatusCode(), closeFrame.getReasonText());
		} else if (frame instanceof BinaryWebSocketFrame) {
			if(!((BinaryWebSocketFrame) frame).isFinalFragment()){
				this.logger.warn("received a frame that not final fragment, not support!");
				return;
			}
			// TODO:oncehandler need broadcast?
			ChannelHandler handler = this.onceHandlers.isEmpty() ? this.channelHandler : this.onceHandlers.poll();
			if (handler != null) {
				ChannelBuffer buffer = ((BinaryWebSocketFrame) frame).getBinaryData();
				handler.onReceive(buffer.array(),
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

	private void clear(ChannelHandlerContext ctx) {
		ctx.getChannel().close();
		if (this.clearHandler != null)
			this.clearHandler.clear();
	}

	private void notifyHandshake() {
		synchronized (this.handshaker) {
			this.handshaker.notify();
		}
	}

	public interface ClearHandler {
		public void clear();
	}
}