package com.taobao.top.link.websocket;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
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
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
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
import org.jboss.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.jboss.netty.util.CharsetUtil;

import com.taobao.top.link.ServerChannel;

public class WebSocketServerChannel extends ServerChannel {

	private String ip;
	private int port;

	public WebSocketServerChannel(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public String getUrl() {
		return String.format("ws://%s:%s", ip, port);
	}

	@Override
	protected void run() {
		ServerBootstrap bootstrap_back = new ServerBootstrap(
				new NioServerSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));
		bootstrap_back.setPipelineFactory(new WebSocketServerPipelineFactory(this.getUrl()));
		bootstrap_back.bind(new InetSocketAddress(this.port));
	}

	public class WebSocketServerPipelineFactory implements
			ChannelPipelineFactory {
		private String url;

		public WebSocketServerPipelineFactory(String url) {
			this.url = url;
		}

		public ChannelPipeline getPipeline() throws Exception {
			ChannelPipeline pipeline = Channels.pipeline();
			pipeline.addLast("decoder", new HttpRequestDecoder());
			pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
			pipeline.addLast("encoder", new HttpResponseEncoder());
			pipeline.addLast("handler", new WebSocketServerHandler(this.url));
			return pipeline;
		}
	}

	public class WebSocketServerHandler extends SimpleChannelUpstreamHandler {
		private String url;
		private WebSocketServerHandshaker handshaker;

		public WebSocketServerHandler(String url) {
			this.url = url;
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
				throws Exception {
			Object msg = e.getMessage();
			if (msg instanceof HttpRequest) {
				handleHttpRequest(ctx, (HttpRequest) msg);
			} else if (msg instanceof WebSocketFrame) {
				handleWebSocketFrame(ctx, (WebSocketFrame) msg);
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
				throws Exception {
			// Throwable error = e.getCause();
		}

		private void handleHttpRequest(ChannelHandlerContext ctx,
				HttpRequest req) throws Exception {
			if (req.getMethod() != HttpMethod.GET) {
				sendHttpResponse(ctx, req,
						new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN));
				return;
			}

			// TODO: get identity info from req
			// origin

			String subprotocols = null;
			WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
					this.url, subprotocols, false);
			handshaker = wsFactory.newHandshaker(req);

			if (handshaker == null) {
				wsFactory.sendUnsupportedWebSocketVersionResponse(ctx.getChannel());
			} else {
				handshaker.handshake(ctx.getChannel(), req).addListener(WebSocketServerHandshaker.HANDSHAKE_LISTENER);
			}
		}

		private void handleWebSocketFrame(ChannelHandlerContext ctx,
				WebSocketFrame frame) {
			if (frame instanceof CloseWebSocketFrame) {
				handshaker.close(ctx.getChannel(), (CloseWebSocketFrame) frame);
				ctx.getChannel().close();
				return;
			} else if (frame instanceof PingWebSocketFrame) {
				return;
			} else if (frame instanceof TextWebSocketFrame) {
				return;
			} else if (frame instanceof BinaryWebSocketFrame) {
				// ((BinaryWebSocketFrame)frame).getBinaryData().
				// TODO: channel uri identity mapping
			} else if (frame instanceof ContinuationWebSocketFrame) {
				return;
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
