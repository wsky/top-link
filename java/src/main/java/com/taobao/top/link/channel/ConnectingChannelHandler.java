package com.taobao.top.link.channel;


public class ConnectingChannelHandler implements ChannelHandler {
	public Throwable error;
	public Object syncObject = new Object();

	@Override
	public void onConnect(ChannelContext context) {
		synchronized (syncObject) {
			syncObject.notify();
		}
	}

	@Override
	public void onMessage(ChannelContext context) {
	}

	@Override
	public void onError(ChannelContext context) {
		error = context.getError();
		synchronized (syncObject) {
			syncObject.notify();
		}
	}

	@Override
	public void onClosed(String reason) {
	}
}