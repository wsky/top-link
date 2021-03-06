package top.link.channel.tcp;

import java.net.URI;

import org.jboss.netty.channel.ChannelPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import top.link.channel.ChannelException;
import top.link.channel.ClientChannel;
import top.link.channel.ConnectingChannelHandler;
import top.link.channel.netty.NettyClient;

public class TcpClient extends NettyClient {
	public static ClientChannel connect(
			URI uri,
			int connectTimeoutMillis,
			ChannelPipeline pipeline)
			throws ChannelException {
		Logger logger = LoggerFactory.getLogger(String.format("TcpClientHandler-%s", uri));
		
		TcpClientChannel clientChannel = new TcpClientChannel();
		clientChannel.setUri(uri);
		
		ConnectingChannelHandler handler = new ConnectingChannelHandler();
		clientChannel.setChannelHandler(handler);
		
		TcpClientUpstreamHandler tcpHandler = new TcpClientUpstreamHandler(logger, clientChannel);
		// connect
		prepareAndConnect(logger, uri,
				pipeline, tcpHandler,
				uri.getScheme().equalsIgnoreCase("ssl"),
				connectTimeoutMillis);
		return clientChannel;
	}
}