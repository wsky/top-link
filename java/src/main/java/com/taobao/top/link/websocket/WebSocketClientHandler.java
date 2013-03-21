package com.taobao.top.link.websocket;

import java.util.Map.Entry;

import org.jboss.netty.buffer.ChannelBuffer;
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

import com.taobao.top.link.Logger;
import com.taobao.top.link.handler.ChannelHandler;

// one handler per connection
public class WebSocketClientHandler extends SimpleChannelUpstreamHandler {
	private static HttpResponseStatus SUCCESS = new HttpResponseStatus(101, "Web Socket Protocol Handshake");

	protected Logger logger;

	protected WebSocketClientHandshaker handshaker;
	protected Throwable failure;

	protected ChannelHandler channelHandler;
	protected ClearHandler clearHandler;

	public WebSocketClientHandler(Logger logger) {
		this.logger = logger;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		if (!this.handshaker.isHandshakeComplete()) {
			this.failure = e.getCause();
			this.notifyHandshake();
		} else {
			this.logger.error("exceptionCaught at client", e.getCause());
			if (this.channelHandler != null)
				this.channelHandler.onException(e.getCause());
		}
		this.clear(ctx);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Object msg = e.getMessage();
		if (!this.handshaker.isHandshakeComplete()) {
			try {
				HttpResponse response = (HttpResponse) e.getMessage();
				this.dump(response);

				boolean validStatus = response.getStatus().equals(SUCCESS);
				boolean validUpgrade = response.getHeader(Names.UPGRADE) != null &&
						response.getHeader(Names.UPGRADE).equalsIgnoreCase(Values.WEBSOCKET);
				boolean validConnection = response.getHeader(Names.CONNECTION) != null &&
						response.getHeader(Names.CONNECTION).equalsIgnoreCase(Values.UPGRADE);

				// TODO:set TopLinkException with errorCode
				if (!validStatus || !validUpgrade || !validConnection) {
					this.failure = new Exception("Invalid handshake response");
				} else {
					this.handshaker.finishHandshake(ctx.getChannel(), response);
				}
			} catch (Exception unknow) {
				this.failure = unknow;
				this.logger.error(unknow);
			} finally {
				this.notifyHandshake();
			}
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
			if (!((BinaryWebSocketFrame) frame).isFinalFragment()) {
				this.logger.warn("received a frame that not final fragment, not support!");
				return;
			}
			if (this.channelHandler != null) {
				ChannelBuffer buffer = ((BinaryWebSocketFrame) frame).getBinaryData();
				this.channelHandler.onReceive(buffer.toByteBuffer(), new WebSocketEndpointContext(ctx));
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

	private void dump(HttpResponse response) {
		if (!this.logger.isDebugEnable())
			return;
		this.logger.debug("%s|%s", 
				response.getStatus().getCode(), 
				response.getStatus().getReasonPhrase());
		for (Entry<String, String> h : response.getHeaders()) {
			this.logger.debug("%s=%s", h.getKey(), h.getValue());
		}
	}

	public interface ClearHandler {
		public void clear();
	}
}