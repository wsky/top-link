package top.link.remoting.netty;

import java.net.URI;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;

import top.link.channel.ChannelException;
import top.link.channel.ClientChannel;
import top.link.channel.ClientChannelSharedSelector;
import top.link.channel.tcp.TcpClient;

public class NettyRemotingClientChannelSharedSelector extends ClientChannelSharedSelector {
	@Override
	protected ClientChannel connect(URI uri, int timeout) throws ChannelException {
		if (uri.getScheme().equalsIgnoreCase("tcp") ||
				uri.getScheme().equalsIgnoreCase("ssl")) {
			ChannelPipeline pipeline = Channels.pipeline();
			pipeline.addLast("decoder", new NettyRemotingDecoder());
			return TcpClient.connect(uri, timeout, pipeline);
		}
		return super.connect(uri, timeout);
	}
}
