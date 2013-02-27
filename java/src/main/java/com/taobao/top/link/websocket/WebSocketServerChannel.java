package com.taobao.top.link.websocket;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.jboss.netty.handler.timeout.IdleState;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.CharsetUtil;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.EndpointContext;
import com.taobao.top.link.Logger;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.ServerChannel;
import com.taobao.top.link.handler.ChannelHandler;

public class WebSocketServerChannel extends ServerChannel {
	private LoggerFactory loggerFactory;
	private Logger logger;

	private String ip;
	private int port;
	private String url;
	private int maxIdleTimeSeconds = 60;

	public WebSocketServerChannel(String ip, int port) {
		this(new DefaultLoggerFactory(), ip, port);
	}

	public WebSocketServerChannel(LoggerFactory factory, String ip, int port) {
		this.loggerFactory = factory;
		this.logger = factory.create(this);
		this.ip = ip;
		this.port = port;
		this.url = String.format("ws://%s:%s/link", this.ip, this.port);
	}

	public String getServerUrl() {
		return this.url;
	}

	public void setMaxIdleTimeSeconds(int value) {
		this.maxIdleTimeSeconds = value;
	}

	@Override
	protected void run() {
		ServerBootstrap bootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));
		// IdleStateHandler.
		// http://docs.jboss.org/netty/3.2/api/org/jboss/netty/handler/timeout/IdleStateHandler.html
		final Timer timer = new HashedWheelTimer();
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast("idleStateHandler", new IdleStateHandler(timer, 0, 0, maxIdleTimeSeconds));
				pipeline.addLast("maxIdleHandler", new MaxIdleHandler(loggerFactory, maxIdleTimeSeconds));
				pipeline.addLast("decoder", new HttpRequestDecoder());
				pipeline.addLast("encoder", new HttpResponseEncoder());
				pipeline.addLast("handler", new WebSocketServerHandler(loggerFactory, url, getChannelHandler()));
				return pipeline;
			}
		});
		bootstrap.bind(new InetSocketAddress(this.port));
		this.logger.info("server channel bind at %s", this.port);
	}

	private static void closeChannel(ChannelHandlerContext ctx, int statusCode, String reason) throws InterruptedException {
		ctx.getChannel().write(new CloseWebSocketFrame(statusCode, reason)).sync();
		ctx.getChannel().close();
	}

	public class MaxIdleHandler extends IdleStateAwareChannelHandler {
		private Logger logger;
		private int maxIdleTimeSeconds;

		public MaxIdleHandler(LoggerFactory loggerFactory, int maxIdleTimeSeconds) {
			this.logger = loggerFactory.create(this);
			this.maxIdleTimeSeconds = maxIdleTimeSeconds;
		}

		@Override
		public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) throws InterruptedException {
			if (e.getState() == IdleState.ALL_IDLE) {
				closeChannel(ctx, 1011, "reach max idle time");
				this.logger.info("reach maxIdleTimeSeconds=%s, close client channel", this.maxIdleTimeSeconds);
			}
		}
	}

	// one handler per connection
	public class WebSocketServerHandler extends SimpleChannelUpstreamHandler {
		private Logger logger;
		private String url;
		private ChannelHandler handler;
		private WebSocketServerHandshaker handshaker;

		public WebSocketServerHandler(LoggerFactory loggerFactory, String url, ChannelHandler handler) {
			this.logger = loggerFactory.create(this);
			this.url = url;
			this.handler = handler;
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
			if (this.handler != null)
				this.handler.onException(e.getCause());
			// TODO:when to send close frame?
			// http://docs.jboss.org/netty/3.2/api/org/jboss/netty/channel/ChannelStateEvent.html
			e.getChannel().close();
		}

		private void handleHttpRequest(ChannelHandlerContext ctx,
				HttpRequest req) throws Exception {
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
			} else {
				this.handshaker.handshake(ctx.getChannel(),
						req).addListener(WebSocketServerHandshaker.HANDSHAKE_LISTENER);
			}
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
					this.handler.onReceive(buffer.array(),
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
}
