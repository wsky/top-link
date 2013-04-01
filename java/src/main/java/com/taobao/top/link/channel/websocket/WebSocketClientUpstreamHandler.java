package com.taobao.top.link.channel.websocket;

import java.util.Map.Entry;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
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

import com.taobao.top.link.LinkException;
import com.taobao.top.link.Logger;
import com.taobao.top.link.channel.ChannelContext;
import com.taobao.top.link.channel.ChannelHandler;

// one handler per connection
public class WebSocketClientUpstreamHandler extends SimpleChannelUpstreamHandler {
	private static HttpResponseStatus SUCCESS = new HttpResponseStatus(101, "Web Socket Protocol Handshake");

	private Logger logger;
	private WebSocketClientChannel clientChannel;
	protected WebSocketClientHandshaker handshaker;

	public WebSocketClientUpstreamHandler(Logger logger, WebSocketClientChannel clientChannel) {
		this.logger = logger;
		this.clientChannel = clientChannel;
	}

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
		this.clientChannel.channel = ctx.getChannel();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		if (this.haveHandler())
			this.getHandler().onError(this.createContext(e.getCause()));
		this.clear(ctx);
		this.logger.error("exceptionCaught at client", e.getCause());
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		if (!this.handshaker.isHandshakeComplete())
			this.handleHandshake(ctx, (HttpResponse) e.getMessage());
		if (e.getMessage() instanceof WebSocketFrame)
			this.handleWebSocketFrame(ctx, (WebSocketFrame) e.getMessage());
	}

	private void handleHandshake(ChannelHandlerContext ctx, HttpResponse response)
			throws Exception {
		this.dump(response);
		boolean validStatus = response.getStatus().equals(SUCCESS);
		boolean validUpgrade = response.getHeader(Names.UPGRADE) != null &&
				response.getHeader(Names.UPGRADE).equalsIgnoreCase(Values.WEBSOCKET);
		boolean validConnection = response.getHeader(Names.CONNECTION) != null &&
				response.getHeader(Names.CONNECTION).equalsIgnoreCase(Values.UPGRADE);

		if (!validStatus || !validUpgrade || !validConnection) {
			throw new LinkException("Invalid handshake response");
		}

		this.handshaker.finishHandshake(ctx.getChannel(), response);
		if (this.haveHandler())
			this.getHandler().onConnect(this.createContext(response));
	}

	private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame)
			throws Exception {
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
			if (this.haveHandler()) {
				ChannelBuffer buffer = ((BinaryWebSocketFrame) frame).getBinaryData();
				this.getHandler().onMessage(this.createContext(buffer.toByteBuffer()));
			}
		}
	}

	private void clear(ChannelHandlerContext ctx) {
		ctx.getChannel().close();
	}

	private boolean haveHandler() {
		return this.clientChannel != null &&
				this.clientChannel.getChannelHandler() != null;
	}

	private ChannelHandler getHandler() {
		return this.clientChannel.getChannelHandler();
	}

	private ChannelContext createContext(Object message) {
		ChannelContext ctx = new ChannelContext();
		ctx.setSender(this.clientChannel);
		ctx.setMessage(message);
		return ctx;
	}

	private ChannelContext createContext(Throwable error) {
		ChannelContext ctx = new ChannelContext();
		ctx.setSender(this.clientChannel);
		ctx.setError(error);
		return ctx;
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