package com.taobao.top.link.channel.netty;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.taobao.top.link.DefaultLoggerFactory;

public class NettyServerTest {
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
