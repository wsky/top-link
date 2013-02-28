package com.taobao.top.link.websocket;

import com.taobao.top.link.OutcomeClientChannel;
import com.taobao.top.link.handler.ChannelHandler;

@Deprecated
public class WebSocketOutcomeClientChannel extends OutcomeClientChannel {
	
	@Override
	public void send(byte[] data, int offset, int length) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setChannelHandler(ChannelHandler handler) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void addOnceChannelHandler(ChannelHandler handler) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

}
