package com.taobao.top.link.remoting;

import java.util.HashMap;

// high-level abstract remoting server
public class DefaultRemotingServerChannelHandler extends RemotingServerChannelHandler {
	@Override
	public MethodReturn onMethodCall(MethodCall methodCall) {
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
