package com.taobao.top.link.websocket;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
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
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.jboss.netty.util.CharsetUtil;

import com.taobao.top.link.EndpointContext;
import com.taobao.top.link.Identity;
import com.taobao.top.link.ServerChannel;
import com.taobao.top.link.handler.ChannelHandler;

public class WebSocketServerChannel extends ServerChannel {

	private String ip;
	private int port;

	public WebSocketServerChannel(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public String getUrl() {
		return String.format("ws://%s:%s/ws", this.ip, this.port);
	}

	@Override
	protected void run() {
		ServerBootstrap bootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast("decoder", new HttpRequestDecoder());
				pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
				pipeline.addLast("encoder", new HttpResponseEncoder());
				pipeline.addLast("handler", new WebSocketServerHandler(getUrl(), getChannelHandler()));
				return pipeline;
			}
		});
		//bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.bind(new InetSocketAddress(this.port));
	}

	// one handler per connection
	public class WebSocketServerHandler extends SimpleChannelUpstreamHandler {
		private String url;
		private ChannelHandler handler;
		private Identity identity;
		private WebSocketServerHandshaker handshaker;

		public WebSocketServerHandler(String url, ChannelHandler handler) {
			this.url = url;
			this.handler = handler;
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
				throws Exception {
			Object msg = e.getMessage();
			System.out.println("--server:" + msg);
			if (msg instanceof HttpRequest) {
				this.handleHttpRequest(ctx, (HttpRequest) msg);
			} else if (msg instanceof WebSocketFrame) {
				this.handleWebSocketFrame(ctx, (WebSocketFrame) msg);
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
				throws Exception {
			// Throwable error = e.getCause();
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
			} else if (frame instanceof BinaryWebSocketFrame) {
				ChannelBuffer buffer = ((BinaryWebSocketFrame) frame).getBinaryData();
				if (this.identity == null) {
					this.identity = this.handler.receiveHandshake(
							buffer.array(), buffer.arrayOffset(), buffer.capacity());
					if (this.identity == null)
						this.handshaker.close(ctx.getChannel(), new CloseWebSocketFrame(401, "unauthorized"));
				} else {
					final Channel channel = ctx.getChannel();
					this.handler.onReceive(buffer.array(),
							buffer.arrayOffset(),
							buffer.capacity(),
							new EndpointContext() {
								@Override
								public void reply(byte[] data, int offset, int length) {
									ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(data, offset, length);
									BinaryWebSocketFrame dataBinaryWebSocketFrame = new BinaryWebSocketFrame(buffer);
									dataBinaryWebSocketFrame.setFinalFragment(true);
									try {
										channel.write(dataBinaryWebSocketFrame).sync();
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
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
