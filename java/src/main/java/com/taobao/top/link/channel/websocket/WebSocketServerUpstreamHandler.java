package com.taobao.top.link.channel.websocket;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocket13FrameDecoder;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.jboss.netty.util.CharsetUtil;

import com.taobao.top.link.Logger;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.Text;
import com.taobao.top.link.channel.ChannelContext;
import com.taobao.top.link.channel.ChannelHandler;
import com.taobao.top.link.channel.ChannelSender;

//one handler per connection
public class WebSocketServerUpstreamHandler extends SimpleChannelUpstreamHandler {
	private Logger logger;
	private ChannelHandler channelHandler;
	private WebSocketServerHandshaker handshaker;
	private ChannelGroup allChannels;
	private ChannelSender sender;
	private boolean cumulative;

	public WebSocketServerUpstreamHandler(LoggerFactory loggerFactory,
			ChannelHandler channelHandler,
			ChannelGroup channelGroup,
			boolean cumulative) {
		this.logger = loggerFactory.create(this);
		this.channelHandler = channelHandler;
		this.allChannels = channelGroup;
		this.cumulative = cumulative;
	}

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
		this.allChannels.add(e.getChannel());
		this.sender = new WebSocketChannelSender(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		if (this.channelHandler != null)
			this.channelHandler.onError(this.createContext(e.getCause()));

		// TODO:when to send close frame?
		// http://docs.jboss.org/netty/3.2/api/org/jboss/netty/channel/ChannelStateEvent.html
		e.getChannel().close();

		this.logger.error(Text.WS_ERROR_AT_SERVER, e.getCause());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Object msg = e.getMessage();
		System.out.println(msg);
		if (msg instanceof HttpRequest) {
			this.handleHttpRequest(ctx, (HttpRequest) msg);
		} else if (msg instanceof WebSocketFrame) {
			this.handleWebSocketFrame(ctx, (WebSocketFrame) msg);
		} else if (msg instanceof List<?>) {
			this.handleWebSocketFrame(ctx, (List<WebSocketFrame>) msg);
		}
	}

	private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest req) throws Exception {
		this.dump(req);

		if (req.getMethod() != HttpMethod.GET) {
			sendHttpResponse(ctx, req,
					new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN));
			return;
		}

		String subprotocols = "mqtt";
		boolean allowExtensions = false;
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
				req.getUri(), subprotocols, allowExtensions);
		this.handshaker = wsFactory.newHandshaker(req);
		if (this.handshaker == null) {
			wsFactory.sendUnsupportedWebSocketVersionResponse(ctx.getChannel());
			return;
		}

		// FIXME: maybe not finish for later work
		this.handshaker.handshake(ctx.getChannel(),
				req).addListener(WebSocketServerHandshaker.HANDSHAKE_LISTENER);

		if (this.cumulative)
			// use custom decoder for cumulative
			ctx.getPipeline().replace(WebSocket13FrameDecoder.class, "wsdecoder-custom",
					new CustomWebSocket13FrameDecoder(true,
							allowExtensions,
							this.handshaker.getMaxFramePayloadLength()));

		if (this.channelHandler != null) {
			this.channelHandler.onConnect(this.createContext(req.getHeaders()));
		}
	}

	private void handleWebSocketFrame(final ChannelHandlerContext ctx,
			WebSocketFrame frame) throws Exception {
		if (frame instanceof CloseWebSocketFrame) {
			ctx.getChannel().close();
			return;
		} else if (frame instanceof BinaryWebSocketFrame) {
			if (!((BinaryWebSocketFrame) frame).isFinalFragment()) {
				this.logger.warn(Text.WS_NOT_FINAL);
				return;
			}
			if (this.channelHandler != null) {
				// if not final frame,
				// should wait until final frame received
				// https://github.com/wsky/top-link/issues/5
				ChannelBuffer buffer = ((BinaryWebSocketFrame) frame).getBinaryData();
				this.channelHandler.onMessage(this.createContext(buffer.toByteBuffer()));
			}
		}
	}

	private void handleWebSocketFrame(final ChannelHandlerContext ctx,
			List<WebSocketFrame> frames) throws Exception {
		List<ByteBuffer> buffers = new ArrayList<ByteBuffer>();
		for (WebSocketFrame f : frames) {
			buffers.add(f.getBinaryData().toByteBuffer());
		}
		this.channelHandler.onMessage(this.createContext(buffers));
	}

	private void sendHttpResponse(ChannelHandlerContext ctx,
			HttpRequest req, HttpResponse res) {
		if (res.getStatus().getCode() != 200) {
			res.setContent(ChannelBuffers.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8));
			HttpHeaders.setContentLength(res, res.getContent().readableBytes());
		}

		ChannelFuture f = ctx.getChannel().write(res);

		if (res.getStatus().getCode() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}

	private ChannelContext createContext(Object message) {
		ChannelContext ctx = new ChannelContext();
		ctx.setSender(this.sender);
		ctx.setMessage(message);
		return ctx;
	}

	private ChannelContext createContext(Throwable error) {
		ChannelContext ctx = new ChannelContext();
		ctx.setSender(this.sender);
		ctx.setError(error);
		return ctx;
	}

	private void dump(HttpRequest request) {
		if (!this.logger.isDebugEnable())
			return;
		this.logger.debug(request.getMethod().getName());
		this.logger.debug(request.getUri());
		for (Entry<String, String> h : request.getHeaders()) {
			this.logger.debug("%s=%s", h.getKey(), h.getValue());
		}
	}
}
