package com.taobao.top.link.websocket;

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
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.jboss.netty.util.CharsetUtil;

import com.taobao.top.link.ChannelSender;
import com.taobao.top.link.Endpoint;
import com.taobao.top.link.EndpointProxy;
import com.taobao.top.link.Identity;
import com.taobao.top.link.LinkException;
import com.taobao.top.link.Logger;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.handler.ChannelHandler;

//one handler per connection
public class WebSocketServerHandler extends SimpleChannelUpstreamHandler {
	private Logger logger;
	private String url;
	private ChannelHandler handler;
	private WebSocketServerHandshaker handshaker;
	private ChannelGroup allChannels;

	private Endpoint endpoint;
	private EndpointProxy endpointProxy;
	private ChannelSender sender;

	public WebSocketServerHandler(LoggerFactory loggerFactory,
			Endpoint endpoint,
			String url,
			ChannelHandler handler,
			ChannelGroup channelGroup) {
		this.logger = loggerFactory.create(this);
		this.endpoint = endpoint;
		this.url = url;
		this.handler = handler;
		this.allChannels = channelGroup;
	}

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
		this.allChannels.add(e.getChannel());
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Object msg = e.getMessage();
		if (msg instanceof HttpRequest) {
			this.handleHttpRequest(ctx, (HttpRequest) msg);
		} else if (msg instanceof WebSocketFrame) {
			this.handleWebSocketFrame(ctx, (WebSocketFrame) msg);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		this.logger.error("exceptionCaught at server", e.getCause());

		if (this.endpointProxy != null)
			this.endpointProxy.remove(this.sender);

		if (this.handler != null)
			this.handler.onException(e.getCause());

		// TODO:when to send close frame?
		// http://docs.jboss.org/netty/3.2/api/org/jboss/netty/channel/ChannelStateEvent.html
		e.getChannel().close();
	}

	private void handleHttpRequest(ChannelHandlerContext ctx,
			HttpRequest req) {
		if (req.getMethod() != HttpMethod.GET) {
			sendHttpResponse(ctx, req,
					new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN));
			return;
		}

		String subprotocols = "mqtt";
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
				this.url, subprotocols, false);
		this.handshaker = wsFactory.newHandshaker(req);
		if (this.handshaker == null) {
			wsFactory.sendUnsupportedWebSocketVersionResponse(ctx.getChannel());
			return;
		}

		Identity identity = null;
		if (this.endpoint.getIdentity() != null) {
			try {
				identity = this.endpoint.getIdentity().parse(req.getHeaders());
			} catch (LinkException e) {
				HttpResponse res = new DefaultHttpResponse(
						HttpVersion.HTTP_1_1,
						HttpResponseStatus.UNAUTHORIZED);
				res.setStatus(new HttpResponseStatus(e.getErrorCode(), e.getMessage()));
				ctx.getChannel().write(res);
				this.logger.error("get identity error", e);
			}
		}

		// create EndpointProxy for income connection
		if (identity != null) {
			this.endpointProxy = this.endpoint.getEndpoint(identity);
			this.endpointProxy.add(this.sender = new WebSocketChannelSender(ctx));
		}

		this.handshaker.handshake(ctx.getChannel(),
				req).addListener(WebSocketServerHandshaker.HANDSHAKE_LISTENER);
	}

	private void handleWebSocketFrame(final ChannelHandlerContext ctx,
			WebSocketFrame frame) {
		if (frame instanceof CloseWebSocketFrame) {
			ctx.getChannel().close();
			return;
		} else if (frame instanceof BinaryWebSocketFrame) {
			if (!((BinaryWebSocketFrame) frame).isFinalFragment()) {
				this.logger.warn("received a frame that not final fragment, not support!");
				return;
			}
			if (this.handler != null) {
				// if not final frame,
				// should wait until final frame received
				// https://github.com/wsky/top-link/issues/5
				ChannelBuffer buffer = ((BinaryWebSocketFrame) frame).getBinaryData();
				this.handler.onReceive(buffer.toByteBuffer(), new WebSocketEndpointContext(ctx));
			}
		}

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
}
