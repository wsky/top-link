package com.taobao.top.link.websocket;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpHeaders.Values;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameDecoder;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameEncoder;
import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketVersion;

import com.taobao.top.link.ChannelException;
import com.taobao.top.link.ClientChannel;
import com.taobao.top.link.EndpointContext;
import com.taobao.top.link.handler.ChannelHandler;
import com.taobao.top.link.handler.ChannelSelectHandler;

public class WebSocketChannelSelectHandler implements ChannelSelectHandler {

	@Override
	public ClientChannel getClientChannel(URI uri) throws ChannelException {
		String scheme = uri.getScheme();

		if (!scheme.equalsIgnoreCase("ws")) {
			return null;
		}

		ClientChannel channel = ChannelHolder.get(uri);
		if (channel != null) {
			return channel;
		}

		WebSocketClientHandshakerFactory wsFactory = new WebSocketClientHandshakerFactory();
		WebSocketClientHandshaker handshaker = wsFactory.newHandshaker(uri, WebSocketVersion.V13, "mqtt", true, null);

		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));
		
		final ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("decoder", new HttpResponseDecoder());
		pipeline.addLast("encoder", new HttpRequestEncoder());
		//pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
		WebSocketClientHandler handler=new WebSocketClientHandler(null);
		pipeline.addLast("handler", handler);
		
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				return pipeline;
			}
		});
		final ChannelFuture future = bootstrap.connect(new InetSocketAddress(uri.getHost(), uri.getPort()));
		try {
			handler.handshaker=handshaker;
			handler.handshakeFuture=	handshaker.handshake(future.getChannel()).sync();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return new ClientChannel() {

			@Override
			public void setUri(URI uri) {
			}

			@Override
			protected void setChannelHandler(ChannelHandler handler) {
			}

			@Override
			public void send(byte[] data, int offset, int length) {
				ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(data, offset, length);
				BinaryWebSocketFrame dataBinaryWebSocketFrame = new BinaryWebSocketFrame(buffer);
				dataBinaryWebSocketFrame.setFinalFragment(true);
				future.getChannel().write(dataBinaryWebSocketFrame);
			}

			@Override
			public URI getUri() {
				return null;
			}

			@Override
			protected void connect() {

			}
		};
	}

	// one handler per connection
	public class WebSocketClientHandler extends SimpleChannelUpstreamHandler {
		private ChannelHandler handler;
		public WebSocketClientHandshaker handshaker;
		public ChannelFuture handshakeFuture;
		private boolean flag;

		public WebSocketClientHandler(ChannelHandler handler) {
			this.handler = handler;
		}
		
		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
				throws Exception {
			Object msg = e.getMessage();

			if (!handshaker.isHandshakeComplete()) {
				System.out.println("--client handshake:" + msg);
				HttpResponse response = (HttpResponse) e.getMessage();
				final HttpResponseStatus status = new HttpResponseStatus(101, "Web Socket Protocol Handshake");
				final boolean validStatus = response.getStatus().equals(status);
				final boolean validUpgrade = response.getHeader(Names.UPGRADE).equalsIgnoreCase(Values.WEBSOCKET);
				final boolean validConnection = response.getHeader(Names.CONNECTION).equalsIgnoreCase(Values.UPGRADE);

				if (!validStatus || !validUpgrade || !validConnection) {
					throw new Exception("Invalid handshake response");
				}

				//flag = true;
				handshaker.finishHandshake(ctx.getChannel(), response);
				handshakeFuture.setSuccess();
				 //ctx.getPipeline().replace("encoder", "ws-encoder", new WebSocketFrameEncoder());
				//ctx.getPipeline().replace("decoder", "ws-decoder", new WebSocketFrameDecoder());
				return;
			}

			if (msg instanceof WebSocketFrame) {
				System.out.println("--client:" + msg);
				this.handleWebSocketFrame(ctx, (WebSocketFrame) msg);
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
				throws Exception {
			 e.getCause().printStackTrace();

		        if (!handshakeFuture.isDone()) {
		            handshakeFuture.setFailure(e.getCause());
		        }
		}

		private void handleWebSocketFrame(final ChannelHandlerContext ctx,
				WebSocketFrame frame) {
			if (frame instanceof CloseWebSocketFrame) {
				ctx.getChannel().close();
				return;
			} else if (frame instanceof BinaryWebSocketFrame) {
				ChannelBuffer buffer = ((BinaryWebSocketFrame) frame).getBinaryData();
				
				String dataString = new String(buffer.array(),
						buffer.arrayOffset(),
						buffer.capacity());
				System.out.println("client receive:" + dataString);
				
				if (this.handler != null)
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
