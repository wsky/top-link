package com.taobao.top.link.websocket;

import static org.junit.Assert.assertEquals;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpClientCodec;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpHeaders.Values;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameDecoder;
import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.junit.Test;

import com.sun.org.apache.bcel.internal.generic.NEW;
import com.taobao.top.link.Endpoint;
import com.taobao.top.link.EndpointContext;
import com.taobao.top.link.Identity;
import com.taobao.top.link.handler.ChannelHandler;

public class ClientTest {
	static Object obj = new Object();

	@Test
	public void connect_test() throws URISyntaxException, InterruptedException {
		URI uri = new URI("ws://localhost:8080/frontend");

		WebSocketClientHandshakerFactory wsFactory = new WebSocketClientHandshakerFactory();
		WebSocketClientHandshaker handshaker = wsFactory.newHandshaker(uri, WebSocketVersion.V13, "default", true, null);

		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast("decoder", new HttpResponseDecoder());
				pipeline.addLast("encoder", new HttpRequestEncoder());
				pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
				pipeline.addLast("handler", new WebSocketClientHandler(null));
				return pipeline;
			}
		});
		Channel channel = bootstrap.connect(new InetSocketAddress(uri.getHost(), uri.getPort())).sync().getChannel();
		try {
			handshaker.handshake(channel).sync();// .awaitUninterruptibly();
		} catch (Exception e) {
			e.printStackTrace();
		}
		synchronized (obj) {
			obj.wait();
		}

		ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(new byte[] { 0, 1 }, 0, 2);
		BinaryWebSocketFrame dataBinaryWebSocketFrame = new BinaryWebSocketFrame(buffer);
		channel.write(dataBinaryWebSocketFrame);
		Thread.sleep(1000);
	}

	// one handler per connection
	public class WebSocketClientHandler extends SimpleChannelUpstreamHandler {
		private ChannelHandler handler;
		private WebSocketServerHandshaker handshaker;
		private boolean flag;

		public WebSocketClientHandler(ChannelHandler handler) {
			this.handler = handler;
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
				throws Exception {
			Object msg = e.getMessage();
			System.out.println(msg);
			if (!flag) {
				HttpResponse response = (HttpResponse) e.getMessage();
				final HttpResponseStatus status = new HttpResponseStatus(101, "Web Socket Protocol Handshake");

				final boolean validStatus = response.getStatus().equals(status);
				final boolean validUpgrade = response.getHeader(Names.UPGRADE).equals(Values.WEBSOCKET);
				final boolean validConnection = response.getHeader(Names.CONNECTION).equals(Values.UPGRADE);

				if (!validStatus || !validUpgrade || !validConnection) {
					throw new Exception("Invalid handshake response");
				}

				flag = true;
				// ctx.getPipeline().replace("decoder", "ws-decoder", new
				// WebSocketFrameDecoder());
				synchronized (obj) {
					obj.notify();
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
			final Throwable t = e.getCause();
			// callback.onError(t);
			e.getChannel().close();
		}

		private void handleWebSocketFrame(final ChannelHandlerContext ctx,
				WebSocketFrame frame) {
			if (frame instanceof CloseWebSocketFrame) {
				handshaker.close(ctx.getChannel(), (CloseWebSocketFrame) frame);
				ctx.getChannel().close();
				return;
			} else if (frame instanceof BinaryWebSocketFrame) {
				ChannelBuffer buffer = ((BinaryWebSocketFrame) frame).getBinaryData();
				this.handler.onReceive(buffer.array(),
						buffer.arrayOffset(),
						buffer.capacity(),
						new EndpointContext() {
							@Override
							public void reply(byte[] data, int offset, int length) {
								ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(data, offset, length);
								BinaryWebSocketFrame dataBinaryWebSocketFrame = new BinaryWebSocketFrame(buffer);
								ctx.getChannel().write(dataBinaryWebSocketFrame);
							}
						});

			}

		}
	}
}
