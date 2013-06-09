package com.taobao.top.link.channel.tcp;

import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.jboss.netty.channel.ChannelPipeline;

public class TcpServerChannelWrapper extends TcpServerChannel {
	public static SSLContext sslContext;
	static {
		try {
			initSslEngine();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public TcpServerUpstreamHandlerWrapper handlerWrapper;

	public TcpServerChannelWrapper(int port) {
		super(port);
	}

	public void ssl() {
		this.setSSLContext(sslContext);
	}

	@Override
	protected void prepareCodec(ChannelPipeline pipeline) {
		pipeline.addLast("decoder", new Byte4Decoder());
	}
	
	protected TcpServerUpstreamHandler createHandler() {
		return handlerWrapper = new TcpServerUpstreamHandlerWrapper(
				this.loggerFactory,
				this.channelHandler,
				this.allChannels);
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
