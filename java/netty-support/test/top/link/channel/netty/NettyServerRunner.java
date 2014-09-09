package top.link.channel.netty;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import top.link.DefaultLoggerFactory;
import top.link.channel.netty.NettyServerChannel;

public class NettyServerRunner {
	public static void main(String[] args) {
		int prev = Thread.getAllStackTraces().keySet().size();
		System.out.println(prev);
		for (int i = 0; i < 3; i++) {
			new NettyServerChannel(DefaultLoggerFactory.getDefault(), 8000 + i) {
				@Override
				protected void preparePipeline(ChannelPipeline pipeline) {
					pipeline.addLast("handler", new SimpleChannelUpstreamHandler());
				}
			}.run();
		}
		System.out.println(Thread.getAllStackTraces().keySet().size());
	}
}
