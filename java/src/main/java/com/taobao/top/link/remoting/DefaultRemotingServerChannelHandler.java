package com.taobao.top.link.remoting;

import java.nio.ByteBuffer;
import java.util.HashMap;

// high-level abstract remoting server
public class DefaultRemotingServerChannelHandler extends RemotingServerChannelHandler {
	@Override
	public byte[] onRequest(ByteBuffer buffer) {
		// TODO:resolve request by sink
		return null;
	}

	private HashMap<String, Object> services;

	public DefaultRemotingServerChannelHandler() {
		this.services = new HashMap<String, Object>();
	}

	public void addService(Object serviceObject) {
		this.services.put(serviceObject.getClass().getName(), serviceObject);
	}
}
