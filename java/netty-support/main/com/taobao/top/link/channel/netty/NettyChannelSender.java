package com.taobao.top.link.channel.netty;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.channel.Channel;

import com.taobao.top.link.channel.ChannelSender;

public abstract class NettyChannelSender implements ChannelSender {
	protected Channel channel;
	private Map<Object, Object> context;

	public NettyChannelSender(Channel channel) {
		this.channel = channel;
		this.context = new HashMap<Object, Object>();
	}
	
	public Channel getChannel() {
		return this.channel;
	}
	
	@Override
	public SocketAddress getLocalAddress() {
		return this.channel.getLocalAddress();
	}
	
	@Override
	public SocketAddress getRemoteAddress() {
		return this.channel.getRemoteAddress();
	}
	
	@Override
	public Object getContext(Object key) {
		return this.context.get(key);
	}

	@Override
	public void setContext(Object key, Object value) {
		this.context.put(key, value);
	}
}