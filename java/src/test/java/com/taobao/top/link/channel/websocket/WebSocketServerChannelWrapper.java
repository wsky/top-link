package com.taobao.top.link.channel.websocket;

import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.jboss.netty.channel.ChannelPipeline;

import com.taobao.top.link.WebSocketServerUpstreamHandlerWrapper;

public class WebSocketServerChannelWrapper extends WebSocketServerChannel {
	public static SSLContext sslContext;
	static {
		try {
			initSslEngine();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public WebSocketServerUpstreamHandlerWrapper handlerWrapper;

	public WebSocketServerChannelWrapper(int port) {
		super(port);
	}

	public void ssl() {
		this.setSSLContext(sslContext);
	}

	protected void preparePipeline(ChannelPipeline pipeline) {
	}

	protected WebSocketServerUpstreamHandler createHandler() {
		return handlerWrapper = new WebSocketServerUpstreamHandlerWrapper(
				this.loggerFactory,
				this.channelHandler,
				this.allChannels,
				this.cumulative);
	}

	private static void initSslEngine() throws Exception {
		String keyStoreFilePassword = "123456";
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("keystore"),
				keyStoreFilePassword.toCharArray());
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(ks, keyStoreFilePassword.toCharArray());
		sslContext = SSLContext.getInstance("TLS");
		sslContext.init(kmf.getKeyManagers(), null, null);
	}
}