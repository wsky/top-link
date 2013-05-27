package com.taobao.top.link.channel.tcp;

import java.nio.ByteBuffer;

import org.jboss.netty.channel.ChannelHandlerContext;

import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ChannelSender;

public class TcpChannelSender implements ChannelSender {

	public TcpChannelSender(ChannelHandlerContext ctx){
		
	}
	
	@Override
	public void send(byte[] data, int offset, int length) throws ChannelException {

	}

	@Override
	public void send(ByteBuffer dataBuffer, SendHandler sendHandler) throws ChannelException {

	}

}
